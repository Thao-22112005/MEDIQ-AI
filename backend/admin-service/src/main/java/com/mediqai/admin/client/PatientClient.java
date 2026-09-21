package com.mediqai.admin.client;

import com.mediqai.admin.dto.request.PatientRequest;
import com.mediqai.admin.dto.response.PatientResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Component
public class PatientClient {

    private final RestClient restClient;

    public PatientClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8083")
                .build();
    }

    // GET tất cả bệnh nhân
    public List<PatientResponse> getAllPatients(String token) {

        PatientResponse[] response = restClient.get()
                .uri("/api/v1/patients")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(PatientResponse[].class);

        return response != null
                ? Arrays.asList(response)
                : List.of();
    }

    // GET bệnh nhân theo ID
    public PatientResponse getPatientById(Long id, String token) {

        return restClient.get()
                .uri("/api/v1/patients/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(PatientResponse.class);
    }

    // PUT cập nhật bệnh nhân
    public PatientResponse updatePatient(
            Long id,
            PatientRequest request,
            String token
    ) {

        return restClient.put()
                .uri("/api/v1/patients/{id}", id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PatientResponse.class);
    }
}