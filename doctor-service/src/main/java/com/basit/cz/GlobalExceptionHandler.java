package com.basit.cz;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 *
 * Catches exceptions and converts them to proper HTTP responses.
 * Provides consistent error format across the API.
 */
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.timestamp = LocalDateTime.now();
        errorResponse.message = exception.getMessage();

        // Handle different exception types
        if (exception instanceof NotFoundException) {
            errorResponse.status = 404;
            errorResponse.error = "Not Found";
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorResponse)
                    .build();
        }

        if (exception instanceof IllegalArgumentException) {
            errorResponse.status = 400;
            errorResponse.error = "Bad Request";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }

        // Default: Internal Server Error
        errorResponse.status = 500;
        errorResponse.error = "Internal Server Error";
        errorResponse.message = "An unexpected error occurred";

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
    }

    /**
     * Error Response DTO
     */
    public static class ErrorResponse {
        public int status;
        public String error;
        public String message;
        public LocalDateTime timestamp;
    }
}








