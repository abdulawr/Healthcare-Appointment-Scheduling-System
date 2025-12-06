package com.example;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handlers for REST API
 */
public class ExceptionMappers {

    /**
     * Handle NotFoundException (404)
     */
    @Provider
    public static class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
        @Override
        public Response toResponse(NotFoundException exception) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Not Found");
            error.put("message", exception.getMessage());
            error.put("status", 404);
            error.put("timestamp", LocalDateTime.now());

            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }
    }

    /**
     * Handle IllegalArgumentException (400 - Bad Request)
     */
    @Provider
    public static class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {
        @Override
        public Response toResponse(IllegalArgumentException exception) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", exception.getMessage());
            error.put("status", 400);
            error.put("timestamp", LocalDateTime.now());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build();
        }
    }

    /**
     * Handle IllegalStateException (409 - Conflict)
     */
    @Provider
    public static class IllegalStateExceptionMapper implements ExceptionMapper<IllegalStateException> {
        @Override
        public Response toResponse(IllegalStateException exception) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Conflict");
            error.put("message", exception.getMessage());
            error.put("status", 409);
            error.put("timestamp", LocalDateTime.now());

            return Response.status(Response.Status.CONFLICT)
                    .entity(error)
                    .build();
        }
    }

    /**
     * Handle Bean Validation Errors (400 - Bad Request)
     */
    @Provider
    public static class ConstraintViolationExceptionMapper
            implements ExceptionMapper<ConstraintViolationException> {
        @Override
        public Response toResponse(ConstraintViolationException exception) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Validation Error");
            error.put("status", 400);
            error.put("timestamp", LocalDateTime.now());

            // Extract validation errors
            String violations = exception.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));

            error.put("message", violations);

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build();
        }
    }

    /**
     * Handle generic exceptions (500 - Internal Server Error)
     */
    @Provider
    public static class GenericExceptionMapper implements ExceptionMapper<Exception> {
        @Override
        public Response toResponse(Exception exception) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", "An unexpected error occurred");
            error.put("status", 500);
            error.put("timestamp", LocalDateTime.now());

            // Log the actual exception (in production)
            exception.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
}



