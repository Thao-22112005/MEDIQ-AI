package com.mediqai.admin.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicalRecordResponse {

    private Long id;
    private String diagnosis;
    private String treatment;
    private String recordDate;
}