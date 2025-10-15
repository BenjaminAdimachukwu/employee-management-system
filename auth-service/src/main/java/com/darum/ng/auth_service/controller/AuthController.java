package com.darum.ng.auth_service.controller;

import com.darum.ng.auth_service.dto.AuthRequest;
import com.darum.ng.auth_service.dto.AuthResponse;
import com.darum.ng.auth_service.dto.TokenValidationResponse;
import com.darum.ng.auth_service.dto.UsernameAvailabilityResponse;
import com.darum.ng.auth_service.entity.User;
import com.darum.ng.auth_service.exception.UserNotFoundException;
import com.darum.ng.auth_service.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse>registerUser(@RequestBody AuthRequest authRequest) {
        logger.info("Registration request for user: {}", authRequest.getUsername());

        if (authRequest.getUsername() == null || authRequest.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (authRequest.getPassword() == null || authRequest.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
// Generate email and set default role
        String email = authRequest.getUsername() + "@zenithCloud.com";
        String role = "ROLE_EMPLOYEE";

        User user = authService.registerUser(
                authRequest.getUsername(),  // 1st - username ✓
                email,                      // 2nd - email ✓
                authRequest.getPassword(),  // 3rd - password ✓
                role                        // 4th - role ✓
        );
        String token = authService.loginUser(authRequest.getUsername(), authRequest.getPassword());

        AuthResponse authResponse = new AuthResponse(
                token,
                user.getUsername(),
                user.getRole().name(),
                "User registered successfully"
        );
        logger.info("User registered successfully: {}", authRequest.getUsername());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
public ResponseEntity<AuthResponse>loginUser(@RequestBody AuthRequest authRequest) {
        logger.info("Login request for user: {}", authRequest.getUsername());

        if (authRequest.getUsername() == null || authRequest.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (authRequest.getPassword() == null || authRequest.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Authenticate user - exceptions will be caught by global handler
        String token = authService.loginUser(authRequest.getUsername(), authRequest.getPassword());

        User user = authService.findByUsername(authRequest.getUsername())
                .orElseThrow(()-> new UserNotFoundException("User not found after successful login"));

        AuthResponse authResponse = new AuthResponse(
                token,
                user.getUsername(),
                user.getRole().name(),
                "Login successful"
        );
        logger.info("Login successful for user: {}", authRequest.getUsername());
        return ResponseEntity.ok(authResponse);
}

@GetMapping("/validate")
public ResponseEntity<TokenValidationResponse>validateToken(@RequestHeader("Authorization") String authHeader) {
    logger.info("Token validation request");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        throw new IllegalArgumentException("Invalid authorization header");
    }
    String token = authHeader.substring(7);
    // Token validation happens in the JWT filter
    // If we reach here, the token is valid

    TokenValidationResponse response = new TokenValidationResponse(
            "Token is Valid"
    );
    logger.info("Token validation successful");
    return ResponseEntity.ok(response);
}

    @GetMapping("/check-username/{username}")
    public ResponseEntity<UsernameAvailabilityResponse> checkUsernameAvailability(@PathVariable String username) {
        logger.info("Username availability check for: {}", username);

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        boolean exists = authService.usernameExists(username);
        String message = exists ? "Username already taken" : "Username available";

        logger.info("Username {} availability: {}", username, exists ? "taken" : "available");
        UsernameAvailabilityResponse response = new UsernameAvailabilityResponse(
                username,
                !exists,
                message
        );
        return ResponseEntity.ok(response);
    }
}
