package com.darum.ng.auth_service.exception;

public class AccountDisabledException extends AuthException {
    public AccountDisabledException(String username) {
        super("Account is disabled: " + username, "ACCOUNT_DISABLED");
    }
}
