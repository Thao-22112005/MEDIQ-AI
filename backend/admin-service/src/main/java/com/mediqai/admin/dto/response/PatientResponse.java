package com.mediqai.admin.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PatientResponse {

    private Long id;
    private String fullName;
    private String gender;
    private String dateOfBirth;
    private String phone;
    private List<MedicalRecordResponse> medicalRecords;
}