package com.darum.ng.auth_service.service.impl;

import com.darum.ng.auth_service.entity.Role;
import com.darum.ng.auth_service.entity.User;
import com.darum.ng.auth_service.exception.AccountDisabledException;
import com.darum.ng.auth_service.exception.GlobalExceptionHandler;
import com.darum.ng.auth_service.exception.InvalidCredentialsException;
import com.darum.ng.auth_service.exception.UserAlreadyExistsException;
import com.darum.ng.auth_service.repository.UserRepository;
import com.darum.ng.auth_service.service.AuthService;
import com.darum.ng.auth_service.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;

    }


    @Override
    public User registerUser(String username, String email, String password, String role) {
        logger.info("Attempting to register user: {}", username);

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        // Check if user already exists
        if (userRepository.existsByUsername(username)) {
            logger.warn("Registration failed: Username {} already exists", username);
            throw new UserAlreadyExistsException("Username", username);
        }
        if (userRepository.existsByEmail(email)) {
            logger.warn("Registration failed: Email {} already exists", email);
            throw new UserAlreadyExistsException("Email", email);
        }

        // Validate password strength
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        // Create new user

        Role roleEnum;
        try {
            roleEnum = Role.valueOf(role.toUpperCase()); // Convert "ROLE_EMPLOYEE" to Role.ROLE_EMPLOYEE
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role + ". Valid roles are: ROLE_ADMIN, ROLE_MANAGER, ROLE_EMPLOYEE");
        }
        User user = new User(username.trim(), email.trim(), passwordEncoder.encode(password), roleEnum);
        User savedUser = userRepository.save(user);

        logger.info("User registered successfully: {}", username);
        return savedUser;
    }

    @Override
    public String loginUser(String username, String password) {
        logger.info("Attempting login for user: {}", username);

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        Optional<User> userOpt = userRepository.findByUsername(username.trim());

        if (userOpt.isEmpty()) {
            logger.warn("Login failed: User {} not found", username);
            throw new InvalidCredentialsException();
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Login failed: Invalid password for user {}", username);
            throw new InvalidCredentialsException();
        }
        if (!user.isEnabled()) {
            logger.warn("Login failed: Account disabled for user {}", username);
            throw new AccountDisabledException(username);
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        logger.info("Login successful for user: {}", username);

        return token;

    }

    @Override
    public Optional<User> findByUsername(String username) {
        // FIX: Actually query the database instead of returning empty
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username.trim());
    }

    @Override
    public boolean usernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByUsername(username.trim());
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok().body(
                new Object() {
                    public final String status = "UP";
                    public final String service = "auth-service";
                    public final String message = "Authentication service is running";
                }
        );
    }
}
