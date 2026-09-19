package com.mediqai.patient.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PatientRequest {
    private String fullName;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;
    private String medicalHistory;
}