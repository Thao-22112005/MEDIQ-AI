package com.mediqai.admin.controller;

import com.mediqai.admin.dto.request.PatientRequest;
import com.mediqai.admin.dto.response.PatientResponse;
import com.mediqai.admin.service.AdminPatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/patients")
@RequiredArgsConstructor
public class AdminPatientController {

    private final AdminPatientService adminPatientService;

    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients(
            @RequestHeader("Authorization") String authorization
    ) {
        String token = authorization.replace("Bearer ", "");

        return ResponseEntity.ok(
                adminPatientService.getAllPatients(token)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization
    ) {
        String token = authorization.replace("Bearer ", "");

        return ResponseEntity.ok(
                adminPatientService.getPatientById(id, token)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long id,
            @RequestBody PatientRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        String token = authorization.replace("Bearer ", "");

        return ResponseEntity.ok(
                adminPatientService.updatePatient(id, request, token)
        );
    }
}