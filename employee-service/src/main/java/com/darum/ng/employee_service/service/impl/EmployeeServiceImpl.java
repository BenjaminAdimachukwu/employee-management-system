package com.darum.ng.employee_service.service.impl;

import com.darum.ng.employee_service.client.AuthServiceClient;
import com.darum.ng.employee_service.dto.EmployeeRequest;
import com.darum.ng.employee_service.dto.EmployeeResponse;
import com.darum.ng.employee_service.dto.UserRegistrationRequest;
import com.darum.ng.employee_service.dto.UserRegistrationResponse;
import com.darum.ng.employee_service.entity.Department;
import com.darum.ng.employee_service.entity.Employee;
import com.darum.ng.employee_service.repository.DepartmentRepository;
import com.darum.ng.employee_service.repository.EmployeeRepository;
import com.darum.ng.employee_service.service.DepartmentService;
import com.darum.ng.employee_service.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private EmployeeRepository employeeRepository;
    private DepartmentRepository departmentRepository;
    private AuthServiceClient  authServiceClient;
    private DepartmentService departmentService;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, AuthServiceClient authServiceClient, DepartmentService departmentService) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.authServiceClient = authServiceClient;
        this.departmentService = departmentService;
    }


    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        logger.info("Creating new employee: {} {}", request.getFirstName(), request.getLastName());

        // 1. Validate department exists using DepartmentService
        if (!departmentService.departmentExists(request.getDepartmentId())) {
            throw new RuntimeException("Department not found with id: " + request.getDepartmentId());
        }

        // 2. Get the department entity
        Department department = departmentRepository.findById(request.getDepartmentId()).orElseThrow(()->
                new RuntimeException("Department not found with id " + request.getDepartmentId()));

        // 3. Check if employee email already exists
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException( "Employee with email" + request.getEmail() + " already exists" );
        }
// 3. Auto-create user in Auth Service using Feign Client
        Long userId = createUserInAuthService(request);

        // 4. Create employee record
        Employee employee = new Employee(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPosition(),     // 4th parameter - position
                request.getSalary(),       // 5th parameter - salary
                request.getPhoneNumber()   // 6th parameter - phoneNumber
        );
employee.setDepartment(department);
        employee.setUserId(userId);
        employee.setStatus(Employee.EmployeeStatus.ACTIVE);

        Employee savedEmployee = employeeRepository.save(employee);
        logger.info("Employee created successfully with ID: {}", savedEmployee.getId());

         return mapToEmployeeResponse(savedEmployee);
    }

    private long createUserInAuthService(EmployeeRequest request) {
        try {
            logger.info("Creating user account in Auth Service for: {}", request.getEmail());

            String temporaryPassword = generateTemporaryPassword();

            // Generate username from email (before @ symbol)
            String username = request.getEmail().split("@")[0];

            UserRegistrationRequest userRequest = new UserRegistrationRequest(
                    username,
                    request.getEmail(),
                    temporaryPassword,
                    request.getFirstName(),
                    request.getLastName()

            );
            // Use Feign Client with ResponseEntity
            ResponseEntity<Map<String, Object>> response = authServiceClient.registerEmployee(userRequest);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                // Extract userId from response - THIS IS THE KEY FIX
//                Long userId = Long.valueOf(responseBody.get("userId").toString());
//
//                logger.info("User account created successfully with ID: {}", userId);
//                return userId;
                // Extract userId from response
                Object userIdObj = responseBody.get("userId");
                if (userIdObj != null) {
                    Long userId = Long.valueOf(userIdObj.toString());
                    logger.info("User account created successfully with ID: {}", userId);
                    return userId;
                } else {
                    throw new RuntimeException("User ID not found in auth service response");
                }

            } else {
                throw new RuntimeException("Auth service returned error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Failed to create user account in Auth Service: {}", e.getMessage());
            throw new RuntimeException("Error creating user account: " + e.getMessage(), e);
        }
    }

    private String generateTemporaryPassword() {
        return "Temp@" + UUID.randomUUID().toString().substring(0 ,8);
    }

    @Override
    public List<EmployeeResponse> getAllEmployees() {
        logger.info("Fetching all employees");
        return employeeRepository.findAll().stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        logger.info("Fetching employee with ID: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Employee with ID: " + id + " not found"));
        return mapToEmployeeResponse(employee);
    }

    @Override
    public EmployeeResponse getEmployeeByUserId(Long userId) {
        logger.info("Fetching employee with user ID: {}", userId);

        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(()-> new RuntimeException("Employee with ID: " + userId + " not found"));

        return  mapToEmployeeResponse(employee);
    }

    @Override
    public List<EmployeeResponse> getEmployeesByDepartment(Long departmentId) {
        logger.info("Fetching employees for department ID: {}", departmentId);
        return employeeRepository.findEmployeesByDepartment(departmentId)
                .stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        logger.info("Updating employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Employee with ID: " + id + " not found"));

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setPosition(request.getPosition());
        employee.setSalary(request.getSalary());

        //  ADD: Handle status update if provided
        if (request.getStatus() != null) {
            try {
                Employee.EmployeeStatus newStatus = Employee.EmployeeStatus.valueOf(request.getStatus().toUpperCase());
                employee.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + request.getStatus());
            }
        }

        // Update department if changed
        if(!employee.getDepartment().getId().equals(request.getDepartmentId())) {
            Department newDepartment = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(()-> new RuntimeException("Department not found with ID: " + request.getDepartmentId()));

            employee.setDepartment(newDepartment);
            logger.info("Employee department updated to: {}", newDepartment.getName());
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        return mapToEmployeeResponse(updatedEmployee);
    }


    @Override
    public List<EmployeeResponse> getEmployeesByStatus(String status) {
        logger.info("Fetching employees with status: {}", status);
        Employee.EmployeeStatus employeeStatus = Employee.EmployeeStatus.valueOf(status.toUpperCase());

        return employeeRepository.findByStatus(employeeStatus).stream()
                .map(this::mapToEmployeeResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteEmployee(Long id) {
        logger.info("Deleting employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Soft delete by changing status
        employee.setStatus(Employee.EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);
        logger.info("Employee marked as INACTIVE");

    }

    // Helper method to convert Entity to Response DTO
    private EmployeeResponse mapToEmployeeResponse(Employee employee) {
        EmployeeResponse employeeResponse = new EmployeeResponse();
        employeeResponse.setId(employee.getId());
        employeeResponse.setFirstName(employee.getFirstName());
        employeeResponse.setLastName(employee.getLastName());
        employeeResponse.setEmail(employee.getEmail());
        employeeResponse.setPhoneNumber(employee.getPhoneNumber());
        employeeResponse.setPosition(employee.getPosition());
        employeeResponse.setSalary(employee.getSalary());
        employeeResponse.setStatus(employee.getStatus());
       employeeResponse.setUserId(employee.getUserId());
       employeeResponse.setCreatedAt(employee.getCreatedAt());
       employeeResponse.setUpdatedAt(employee.getUpdatedAt());

       if(employee.getDepartment() != null) {
           employeeResponse.setDepartmentId(employee.getDepartment().getId());
           employeeResponse.setDepartmentName(employee.getDepartment().getName());
       }
       return employeeResponse;
    }
}
