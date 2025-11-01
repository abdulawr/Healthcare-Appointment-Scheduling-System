# WorkflowHandler

- **Status**: `In Development`
- **Tier Level:** `Tier 1`

---

## Overview

The WorkflowHandler Service manages background processes required for the different use case executions.

### Key Responsibilities:

- Handle whole workflow lifecycle.
- Gracefully break workflow execution in case of the failure (+ retries mechanism).
- Guarantee transactional behavior for the entire workflow.
- Provide configurable logging.
- Provide user-friendly SDK for Java.
- Provide retries mechanism with backoff

---

## Implementation

Described functionality is already implemented by [Temporal (Java SDK)](https://docs.temporal.io/develop/java).
It’s purpose-built for reliable background workflows in Java and cleanly matches every item on our responsibilities list.

### 1) Handle the whole workflow lifecycle

#### What we get:

- Durable execution (state is persisted as an event history). 
- Start/continue/terminate, signals (external events), queries (read state), child workflows, timers, and Continue-As-New for long runners. 
- Task Queues to bind clients ↔ workers and scale out. 
- Deterministic replay (safe restarts/redeploys).

#### Example:

```java
@WorkflowInterface
public interface OrderFlow {
  @WorkflowMethod void run(String orderId);
  @SignalMethod void cancel();
  @QueryMethod String status();
}

public class OrderFlowImpl implements OrderFlow {
  private String state = "NEW";
  private volatile boolean cancelled;

  @Override public void cancel() { cancelled = true; }
  @Override public String status() { return state; }

  @Override
  public void run(String orderId) {
    state = "CHARGING";
    // timers, child workflows, etc. are allowed inside workflows
    Workflow.sleep(Duration.ofMinutes(5)); // example timer
    // ...
    state = "DONE";
  }
}
```

### 2) Gracefully break workflow execution in case of failure

#### What we get:

- Per-step (activity) automatic retries with backoff. 
- Cancellation that propagates (via CancellationScope). 
- Typed failures that surface to the workflow, so you can branch, compensate, or stop cleanly. 
- Heartbeats & timeouts to detect stuck work.

#### Example:

```java
ActivityOptions opts = ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofMinutes(2))
        .setRetryOptions(RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(1))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(6)
                .build())
        .build();

PaymentActivities pay = Workflow.newActivityStub(PaymentActivities.class, opts);

Workflow.wrap(() -> {
        pay.charge(orderId);                 // will retry per policy
}).run(); // Wrap if you want centralized error mapping

// Cooperative cancel:
CancellationScope scope = Workflow.newCancellationScope(() -> {
    pay.charge(orderId);
    ship.reserve(orderId);
});
scope.run();
// elsewhere, when signaled:
scope.cancel();
```

### 3) Guarantee transactional behavior for the entire workflow

#### Reality check

A single ACID transaction across multiple services is not feasible/safe.
Temporal guarantees durable, exactly-once workflow progression; activities are at-least-once, so make them idempotent. 

Use the Saga pattern (compensation) for cross-step consistency.

#### Practical recipe

- Assign a business idempotency key to each side effect (e.g., paymentId).

- In activities, check-and-set using that key; on retry you do nothing twice.

- Register compensations in workflow state and call them on failure/cancel.

#### Example:

```java
public class OrderFlowImpl implements OrderFlow {
    private final PaymentActivities pay = Workflow.newActivityStub(...);
    private final ShippingActivities ship = Workflow.newActivityStub(...);
    private final List<Runnable> compensations = new ArrayList<>();

    @Override
    public void run(String orderId) {
        String paymentId = "pay-" + orderId;
        pay.chargeOnce(paymentId);                // idempotent
        compensations.add(() -> pay.refundOnce(paymentId));

        String reservationId = "res-" + orderId;
        ship.reserveOnce(reservationId);
        compensations.add(() -> ship.releaseOnce(reservationId));

        try {
            ship.dispatch(orderId);
        } catch (Throwable t) {
            // roll back in reverse order
            for (int i = compensations.size()-1; i >= 0; i--) compensations.get(i).run();
            throw t; // Temporal will persist this and you can decide to retry or end
        }
    }
}
```

### 4) Provide configurable logging

#### What we get:

- Use SLF4J/Logback (or your choice) in activities (they run in normal JVM threads). 
- Inside workflows, use `Workflow.getLogger(...)` (log lines correlate with workflowId/runId). 
- Full event history is always available for root-cause analysis; you can also upsert search attributes for indexed metadata (e.g., `customerId`, `orderTotal`).

#### Setup:

```xml
<!-- logback.xml -->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{ISO8601} %-5level [%X{wfId}:%X{runId}] %logger - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="INFO"><appender-ref ref="STDOUT"/></root>
</configuration>
```

#### Use MDC in activities:

```java
try (MDC.MDCCloseable a = MDC.putCloseable("wfId", Workflow.getInfo().getWorkflowId());
     MDC.MDCCloseable b = MDC.putCloseable("runId", Workflow.getInfo().getRunId())) {
  log.info("Charging {}", orderId);
}
```

#### Workflow-side logger:

```java
Workflow.getLogger(OrderFlowImpl.class).info("Step finished: {}", stepName);
```

### 5) Provide a user-friendly SDK for Java

#### What we get:

- Workflows = annotated interfaces/classes; steps are just methods. 
- Activities = plain Java; no framework lock-in. 
- Strong testing tools: `TestWorkflowEnvironment` to run workflows in-memory. 
- Easy local dev: run Temporal via Docker; production via self-hosted or cloud.

#### Spin up a worker:

```java
WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
WorkflowClient client = WorkflowClient.newInstance(service);

WorkerFactory factory = WorkerFactory.newInstance(client);
Worker worker = factory.newWorker("orders-task-queue");
worker.registerWorkflowImplementationTypes(OrderFlowImpl.class);
worker.registerActivitiesImplementations(new PaymentActivitiesImpl(), new ShippingActivitiesImpl());
factory.start();
```

#### Start a workflow:

```java
OrderFlow stub = client.newWorkflowStub(
  OrderFlow.class,
  WorkflowOptions.newBuilder()
    .setTaskQueue("orders-task-queue")
    .setWorkflowId("order-" + orderId)   // business key
    .build());
WorkflowClient.start(stub::run, orderId);
```
