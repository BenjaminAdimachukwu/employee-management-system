package com.darum.ng.employee_service.controller;

import com.darum.ng.employee_service.dto.EmployeeRequest;
import com.darum.ng.employee_service.dto.EmployeeResponse;
import com.darum.ng.employee_service.service.EmployeeService;
import com.darum.ng.employee_service.utils.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final SecurityUtils securityUtils;

    public EmployeeController(EmployeeService employeeService, SecurityUtils securityUtils) {
        this.employeeService = employeeService;
        this.securityUtils = securityUtils;
    }

    // ADMIN ONLY: Create employee
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest employeeRequest) {
        EmployeeResponse employee = employeeService.createEmployee(employeeRequest);
        return ResponseEntity.ok(employee);
    }

    // ADMIN: Get all employees
    // MANAGER: Get employees in their department
    // EMPLOYEE: Get only themselves
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        if (securityUtils.isAdmin()) {
            // Admin sees all employees
            List<EmployeeResponse> employees = employeeService.getAllEmployees();
            return ResponseEntity.ok(employees);

        } else if (securityUtils.isManager()) {
            // Manager sees employees in their department
            // We'll implement this after department-user association
            List<EmployeeResponse> employees = employeeService.getAllEmployees();
            return ResponseEntity.ok(employees);

        } else {
            // Employee sees only themselves
            // We need to implement getCurrentUserEmployee()
            List<EmployeeResponse> employees = Collections.emptyList(); // Temporary
            return ResponseEntity.ok(employees);
        }

    }

    // ADMIN: Get any employee by ID
    // MANAGER: Get employees in their department
    // EMPLOYEE: Get only themselves

    @GetMapping("{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {

        if (!securityUtils.canAccessEmployee(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        EmployeeResponse employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);

    }

    // ADMIN: Get by any user ID
    // EMPLOYEE: Get only their own user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<EmployeeResponse> getEmployeeByUserId(@PathVariable Long userId) {
        if (securityUtils.isAdmin() || securityUtils.isManager()) {
            EmployeeResponse employee = employeeService.getEmployeeByUserId(userId);
            return ResponseEntity.ok(employee);
        } else {

            Long currentEmployeeId = securityUtils.getCurrentUserEmployeeId();
            if (currentEmployeeId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            EmployeeResponse employee = employeeService.getEmployeeById(currentEmployeeId);
            return ResponseEntity.ok(employee);
        }
    }


    // ADMIN: Update any employee
    // MANAGER: Update employees in their department
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest employeeRequest) {

        if (securityUtils.isManager() && !securityUtils.canAccessEmployee(id)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        EmployeeResponse employee = employeeService.updateEmployee(id, employeeRequest);
        return ResponseEntity.ok(employee);

    }

    // ADMIN ONLY: Delete employee
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    // MANAGER & ADMIN: Get department employees
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<EmployeeResponse>> getEmployeeByDepartment(@PathVariable Long departmentId) {
        List<EmployeeResponse> employees = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(employees);
    }

    // ADMIN: Get employees by any status
    // MANAGER: Get employees by status in their department
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeResponse>> getEmployeeByStatus(@PathVariable String status) {

        if (securityUtils.isAdmin()) {
            List<EmployeeResponse> employees = employeeService.getEmployeesByStatus(status);
            return ResponseEntity.ok(employees);

        } else if (securityUtils.isManager()) {
            // Manager sees status-filtered employees in their department
            List<EmployeeResponse> employees = employeeService.getEmployeesByStatus(status);
            return ResponseEntity.ok(employees);
        }  else {
            // Employee can only see their own status (which doesn't make sense for this endpoint)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

    }

}
