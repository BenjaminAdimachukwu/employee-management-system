package com.darum.ng.auth_service.exception;

public class UserNotFoundException extends AuthException{
    public UserNotFoundException(String username) {
        super("User not found: " + username, "USER_NOT_FOUND");
    }
}
