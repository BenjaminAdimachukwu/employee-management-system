package com.darum.ng.employee_service.service;

import com.darum.ng.employee_service.dto.EmployeeRequest;
import com.darum.ng.employee_service.dto.EmployeeResponse;

import java.util.List;

public interface EmployeeService {
    EmployeeResponse createEmployee(EmployeeRequest request);
    List<EmployeeResponse> getAllEmployees();
    EmployeeResponse getEmployeeById(Long id);
    EmployeeResponse getEmployeeByUserId(Long userId);
    List<EmployeeResponse> getEmployeesByDepartment(Long departmentId);
    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);
    List<EmployeeResponse> getEmployeesByStatus(String status);
    void deleteEmployee(Long id);
}
