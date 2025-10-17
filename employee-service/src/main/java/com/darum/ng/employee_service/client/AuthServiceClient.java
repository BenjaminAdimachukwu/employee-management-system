package com.darum.ng.employee_service.client;

import com.darum.ng.employee_service.dto.UserRegistrationRequest;
import com.darum.ng.employee_service.dto.UserRegistrationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "auth-service", url = "${auth.service.url:http://localhost:8081}")
public interface AuthServiceClient {

    @PostMapping("/auth/register/employee")
    ResponseEntity<Map<String, Object>> registerEmployee(@RequestBody UserRegistrationRequest userRegistrationRequest);
}
