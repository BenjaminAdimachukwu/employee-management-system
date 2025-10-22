package com.darum.ng.employee_service.utils;

import com.darum.ng.employee_service.entity.Employee;
import com.darum.ng.employee_service.repository.DepartmentRepository;
import com.darum.ng.employee_service.repository.EmployeeRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
//public class SecurityUtils {

//
//    public String getCurrentUsername() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        return authentication.getName();
//
//        //return SecurityContextHolder.getContext().getAuthentication().getName();
//    }
//
//    public boolean isAdmin() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        return authentication.getAuthorities().stream().anyMatch(authority ->
//                authority
//                        .getAuthority()
//                        .equals("ROLE_ADMIN"));
//    }
//
//    public boolean isManager() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        return authentication.getAuthorities().stream().anyMatch(authority ->
//                authority.getAuthority().equals("ROLE_MANAGER"));
//    }
//
//    public boolean isEmployee() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        return authentication.getAuthorities().stream().anyMatch(authority ->
//                authority.getAuthority().equals("ROLE_EMPLOYEE"));
//    }
//    public boolean isCurrentUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        // This would require a way to get userId from JWT token
//        // For now, we'll handle this differently
//        return true;
//    }


//}


public class SecurityUtils {


    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public SecurityUtils(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }


    public Long getCurrentUserEmployeeId() {
        String username = getCurrentUsername();

        if (username == null) return null;

        try {
            return employeeRepository.findByEmail(username)
                    .map(Employee::getId)
                    .orElse(null);
        } catch (Exception e) {
            return null; // Handle database errors gracefully
        }
    }

    public Long getCurrentUserDepartmentId() {
        Long employeeId = getCurrentUserEmployeeId();
        if (employeeId == null) return null;

        try {
            return employeeRepository.findById(employeeId)
                    .map(employee -> employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    //  COMPLETE: Check if user can access employee
    public boolean canAccessEmployee(Long targetEmployeeId) {
        if (isAdmin()) return true;

        if (isManager()) {
            Long managerDepartmentId = getCurrentUserDepartmentId();
            if (managerDepartmentId == null) return false;

            try {
                return employeeRepository.findById(targetEmployeeId)
                        .map(employee -> employee.getDepartment() != null &&
                                employee.getDepartment().getId().equals(managerDepartmentId)) // ✅ FIXED: Compare department IDs
                        .orElse(false);
            } catch (Exception e) {
                return false;
            }
        }

        if (isEmployee()) {
            Long currentEmployeeId = getCurrentUserEmployeeId();
            return currentEmployeeId != null && currentEmployeeId.equals(targetEmployeeId);
        }
        return false;
    }

    //COMPLETE: Check if user can access department
    public boolean canAccessDepartment(Long targetDepartmentId) {

        if (isAdmin()) return true;

        if (isManager()) {
            Long managerDepartmentId = getCurrentUserDepartmentId();
            return managerDepartmentId != null && managerDepartmentId.equals(targetDepartmentId);
        }
        return false;
    }


    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isManager() {
        return hasRole("ROLE_MANAGER");
    }

    public boolean isEmployee() {
        return hasRole("ROLE_EMPLOYEE");
    }

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role));
    }

    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
    }

    public List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptyList();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}


