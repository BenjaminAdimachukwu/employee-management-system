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

import java.util.HashMap;
import java.util.Map;

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

@PostMapping("/register/employee")
public  ResponseEntity<Map<String, Object>> registerEmployee(@RequestBody  Map<String, Object> request) {

        try {
            String username = request.get("username").toString();
            String email = request.get("email").toString();
            String password = request.get("password").toString();
            String firstName = request.get("firstName").toString();
            String lastName = request.get("lastName").toString();
            String role = "ROLE_EMPLOYEE";
            if( request.containsKey("role") ) {
                role = request.get("role").toString();
            }

            // Validate required fields
            if (username == null || email == null || password == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Missing required fields: username, email, password");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Create user with EMPLOYEE role
            User user = authService.registerUser(username, email, password, role);

            // Return user ID for linking with employee record
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("role", user.getRole().name());
            response.put("message", "Employee User Account registered successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Employee registration failed: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Employee registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
}

    @PostMapping("/register/admin")
    public ResponseEntity<Map<String, Object>> registerAdmin(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> adminRequest = new HashMap<>(request);
            adminRequest.put("role", "ROLE_ADMIN");
            return registerEmployee(adminRequest);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Admin registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/register/manager")
    public ResponseEntity<Map<String, Object>> registerManager(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> managerRequest = new HashMap<>(request);
            managerRequest.put("role", "ROLE_MANAGER");
            return registerEmployee(managerRequest);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Manager registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
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
