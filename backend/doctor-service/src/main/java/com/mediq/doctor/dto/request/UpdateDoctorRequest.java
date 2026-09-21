package com.mediq.doctor.dto.request;

import com.mediq.doctor.entity.DoctorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDoctorRequest {

    private String fullName;
    private DoctorStatus status;
}
