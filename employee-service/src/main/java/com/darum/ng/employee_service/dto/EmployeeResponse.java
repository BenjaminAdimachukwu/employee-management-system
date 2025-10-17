package com.darum.ng.employee_service.dto;

import com.darum.ng.employee_service.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String position;
    private Double salary;
    private Employee.EmployeeStatus status;
    private Long departmentId;
    private String departmentName;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
