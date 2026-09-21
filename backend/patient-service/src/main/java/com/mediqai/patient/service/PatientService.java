package com.mediqai.patient.service;

import com.mediqai.patient.dto.PatientRequest;
import com.mediqai.patient.entity.Patient;
import com.mediqai.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;

    public Patient createPatient(PatientRequest request) {
        Patient patient = new Patient();
        patient.setFullName(request.getFullName());
        patient.setGender(request.getGender());
        patient.setDateOfBirth(request.getDateOfBirth().toString());
        patient.setPhone(request.getPhone());
        return patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy bệnh nhân ID: " + id));
    }

    public Patient updatePatient(Long id, PatientRequest request) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy bệnh nhân ID: " + id));

        patient.setFullName(request.getFullName());
        patient.setPhone(request.getPhone());
        patient.setGender(request.getGender());

        if (request.getDateOfBirth() != null) {
            patient.setDateOfBirth(request.getDateOfBirth().toString());
        }

        return patientRepository.save(patient);
    }
}