package com.basit.cz.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps DuplicateEmailException to HTTP 409 Conflict.
 *
 * When a DuplicateEmailException is thrown (email already exists),
 * this mapper converts it to a proper HTTP response with status 409.
 */
@Provider
public class DuplicateEmailExceptionMapper implements ExceptionMapper<DuplicateEmailException> {

    @Override
    public Response toResponse(DuplicateEmailException exception) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Conflict");
        error.put("message", exception.getMessage());
        error.put("status", 409);

        return Response
                .status(Response.Status.CONFLICT)
                .entity(error)
                .build();
    }
}



