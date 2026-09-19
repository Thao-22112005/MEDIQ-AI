package com.mediqai.patient.service;

import com.mediqai.patient.dto.MedicalRecordRequest;
import com.mediqai.patient.dto.MedicalRecordResponse;
import com.mediqai.patient.entity.MedicalRecord;
import com.mediqai.patient.entity.Patient;
import com.mediqai.patient.repository.MedicalRecordRepository;
import com.mediqai.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;

    public MedicalRecordResponse createMedicalRecord(MedicalRecordRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân ID: " + request.getPatientId()));

        MedicalRecord record = new MedicalRecord();
        record.setDiagnosis(request.getDiagnosis());
        record.setTreatment(request.getTreatment());
        record.setRecordDate(request.getRecordDate());
        record.setPatient(patient);

        MedicalRecord savedRecord = medicalRecordRepository.save(record);
        return mapToResponse(savedRecord);
    }

    public List<MedicalRecordResponse> getRecordsByPatientId(Long patientId) {
        List<MedicalRecord> records = medicalRecordRepository.findByPatientId(patientId);
        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private MedicalRecordResponse mapToResponse(MedicalRecord record) {
        return MedicalRecordResponse.builder()
                .id(record.getId())
                .patientId(record.getPatient().getId())
                .diagnosis(record.getDiagnosis())
                .treatment(record.getTreatment())
                .recordDate(record.getRecordDate())
                .build();
    }
}