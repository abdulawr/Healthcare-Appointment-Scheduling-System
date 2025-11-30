package com.basit.cz.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps PatientNotFoundException to HTTP 404 Not Found.
 *
 * When a PatientNotFoundException is thrown, this mapper converts it
 * to a proper HTTP response with status 404 and a JSON error body.
 */
@Provider
public class PatientNotFoundExceptionMapper implements ExceptionMapper<PatientNotFoundException> {

    @Override
    public Response toResponse(PatientNotFoundException exception) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Not Found");
        error.put("message", exception.getMessage());
        error.put("status", 404);

        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(error)
                .build();
    }
}
