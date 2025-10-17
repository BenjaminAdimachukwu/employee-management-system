package com.darum.ng.employee_service.controller;

import com.darum.ng.employee_service.dto.EmployeeRequest;
import com.darum.ng.employee_service.dto.EmployeeResponse;
import com.darum.ng.employee_service.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(  @Valid @RequestBody EmployeeRequest employeeRequest) {
        EmployeeResponse employee = employeeService.createEmployee(employeeRequest);
        return ResponseEntity.ok(employee);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        List<EmployeeResponse> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById( @PathVariable Long id) {
        EmployeeResponse employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<EmployeeResponse> getEmployeeByUserId(@PathVariable Long userId) {
        EmployeeResponse employee = employeeService.getEmployeeByUserId(userId);
        return ResponseEntity.ok(employee);
    }


    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest employeeRequest) {
EmployeeResponse employee = employeeService.updateEmployee(id, employeeRequest);
return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<EmployeeResponse>> getEmployeeByDepartment(@PathVariable  Long departmentId) {
List<EmployeeResponse> employees = employeeService.getEmployeesByDepartment(departmentId)
        ;
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeResponse>> getEmployeeByStatus(@PathVariable String status) {
        List<EmployeeResponse> employees = employeeService.getEmployeesByStatus(status);
        return ResponseEntity.ok(employees);
    }

}
