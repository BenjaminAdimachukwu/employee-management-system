package com.darum.ng.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsernameAvailabilityResponse {
    private String username;
    private boolean available;
    private String message;
}