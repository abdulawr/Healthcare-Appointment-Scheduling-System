package com.basit.cz.notification.novu;

import java.util.List;
import java.util.Map;

public class NovuTriggerRequest {

    public String name; // workflow trigger identifier
    public String transactionId;

    public static class To {
        public String subscriberId;
        public String email;
        public String phone;
    }

    public List<To> to;
    public Map<String, Object> payload;
}
