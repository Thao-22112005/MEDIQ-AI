package com.mediqai.admin.service;

import com.mediqai.admin.client.PatientClient;
import com.mediqai.admin.dto.request.PatientRequest;
import com.mediqai.admin.dto.response.PatientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPatientService {

    private final PatientClient patientClient;

    public List<PatientResponse> getAllPatients(String token) {
        return patientClient.getAllPatients(token);
    }

    public PatientResponse getPatientById(Long id, String token) {
        return patientClient.getPatientById(id, token);
    }

    public PatientResponse updatePatient(
            Long id,
            PatientRequest request,
            String token
    ) {
        return patientClient.updatePatient(id, request, token);
    }
}