package com.darum.ng.employee_service.service;

import com.darum.ng.employee_service.dto.DepartmentRequest;
import com.darum.ng.employee_service.dto.DepartmentResponse;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse createDepartment(DepartmentRequest request);
    List<DepartmentResponse> getAllDepartments();
    DepartmentResponse getDepartmentById(Long id);
    DepartmentResponse getDepartmentByName(String name);
    DepartmentResponse updateDepartment(Long id, DepartmentRequest request);
    void deleteDepartmentById(Long id);
    boolean departmentExists(Long id);
}
