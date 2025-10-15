package com.darum.ng.auth_service.exception;

public class UserAlreadyExistsException extends AuthException {
    public UserAlreadyExistsException(String field, String value) {
        super(field + " already exists: " + value, "USER_ALREADY_EXISTS");
    }
}
