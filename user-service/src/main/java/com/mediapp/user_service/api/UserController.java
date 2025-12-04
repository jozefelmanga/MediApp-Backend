package com.mediapp.user_service.api;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mediapp.common.dto.ApiResponse;
import com.mediapp.common.dto.PageResponse;
import com.mediapp.user_service.api.dto.DoctorRegistrationRequest;
import com.mediapp.user_service.api.dto.PatientRegistrationRequest;
import com.mediapp.user_service.api.dto.PatientSummaryDto;
import com.mediapp.user_service.api.dto.UserDetailsResponse;
import com.mediapp.user_service.service.UserManagementService;

import jakarta.validation.Valid;

/**
 * REST endpoints for managing MediApp user accounts.
 */
@RestController
@RequestMapping(path = "/api/v1/users")
@Validated
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @PostMapping(path = "/register/patient")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> registerPatient(
            @Valid @RequestBody PatientRegistrationRequest request) {
        UserDetailsResponse response = userManagementService.registerPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping(path = "/register/doctor")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> registerDoctor(
            @RequestHeader("X-Admin-Token") String adminToken,
            @Valid @RequestBody DoctorRegistrationRequest request) {
        UserDetailsResponse response = userManagementService.registerDoctor(adminToken, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping(path = "/details/{userId}")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> getUserDetails(@PathVariable UUID userId) {
        UserDetailsResponse response = userManagementService.getUserDetails(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping(path = "/all/patients")
    public ResponseEntity<ApiResponse<PageResponse<PatientSummaryDto>>> listPatients(
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<PatientSummaryDto> response = userManagementService.listPatients(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
