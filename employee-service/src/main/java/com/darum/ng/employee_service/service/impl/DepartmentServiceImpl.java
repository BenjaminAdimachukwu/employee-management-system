package com.darum.ng.employee_service.service.impl;

import com.darum.ng.employee_service.dto.DepartmentRequest;
import com.darum.ng.employee_service.dto.DepartmentResponse;
import com.darum.ng.employee_service.entity.Department;
import com.darum.ng.employee_service.repository.DepartmentRepository;
import com.darum.ng.employee_service.repository.EmployeeRepository;
import com.darum.ng.employee_service.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);

private DepartmentRepository departmentRepository;
private EmployeeRepository employeeRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        logger.info("Creating new department: {}", request.getName());
        if (departmentRepository.existsByName(request.getName())) {
            throw new RuntimeException("Department with name '" + request.getName() + "' already exists");
        }
        Department department = new Department(request.getName(), request.getDescription());
        Department savedDepartment = departmentRepository.save(department);
        logger.info("Department created successfully with ID: {}", savedDepartment.getId());

        return mapToDepartmentResponse(savedDepartment);
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        logger.info("Fetching all departments");
        return departmentRepository.findAll()
                .stream()
                .map(this::mapToDepartmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentResponse getDepartmentById(Long id) {
        logger.info("Fetching department with ID: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        return mapToDepartmentResponse(department);
    }

    @Override
    public DepartmentResponse getDepartmentByName(String name) {
        logger.info("Fetching department with name: {}", name);

        Department department = departmentRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Department not found with name: " + name));
        return mapToDepartmentResponse(department);
    }

    @Override
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        logger.info("Updating department with ID: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
// Check if new name conflicts with existing department
        if (!department.getName().equals(request.getName()) &&
                departmentRepository.existsByName(request.getName())) {
            throw new RuntimeException("Department with name '" + request.getName() + "' already exists");
        }

        department.setName(request.getName());
        department.setDescription(request.getDescription());

        Department updatedDepartment = departmentRepository.save(department);
        return mapToDepartmentResponse(updatedDepartment);
    }

    @Override
    public void deleteDepartmentById(Long id) {
        logger.info("Deleting department with ID: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // Check if department has employees
        Long employeeCount = departmentRepository.countEmployeesByDepartment(id);
        if (employeeCount > 0) {
            throw new RuntimeException("Cannot delete department with " + employeeCount + " employees. Reassign employees first.");
        }
        departmentRepository.delete(department);
        logger.info("Department deleted successfully with ID: {}", id);
    }

    @Override
    public boolean departmentExists(Long id) {
        return departmentRepository.existsById(id);
    }
    // Helper method to convert Entity to Response DTO
    private DepartmentResponse mapToDepartmentResponse(Department department) {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(department.getId());
        response.setName(department.getName());
        response.setDescription(department.getDescription());
        response.setCreatedAt(department.getCreatedAt());
        response.setUpdatedAt(department.getUpdatedAt());

        // Count employees in this department
        Long employeeCount = departmentRepository.countEmployeesByDepartment(department.getId());

        response.setEmployeeCount(employeeCount != null ? employeeCount.intValue() : 0);
         return response;
    }
}
