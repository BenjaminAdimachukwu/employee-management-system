package com.darum.ng.auth_service.service;



import com.darum.ng.auth_service.entity.User;

import java.util.Optional;

public interface AuthService {

    /**
     * Register a new user
     */
    User registerUser(String username, String email, String password, String role);

    /**
     * Login user and return JWT token
     */
    String loginUser(String username, String password);

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

//    /**
//     * Validate user credentials
//     */
//    boolean validateCredentials(String username, String password);
//
    /**
     * Check if username exists
     */
    boolean usernameExists(String username);

//    /**
//     * Check if email exists
//    */
//  boolean emailExists(String email);
}