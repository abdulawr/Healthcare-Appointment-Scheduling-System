package com.basit.cz.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("Patient already exists with email: " + email);
    }
}
