package com.darum.ng.auth_service.exception;

public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException() {
        super("Invalid username or password", "INVALID_CREDENTIALS");
    }
}
