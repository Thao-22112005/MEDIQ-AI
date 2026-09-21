package com.mediq.appointment.mapper;

import com.mediq.appointment.dto.response.ReplacementProposalResponse;
import com.mediq.appointment.entity.ReplacementProposal;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReplacementProposalMapper {

    public ReplacementProposalResponse toResponse(ReplacementProposal entity) {
        if (entity == null) {
            return null;
        }
        return new ReplacementProposalResponse(
                entity.getProposalId(),
                entity.getAppointment() != null ? entity.getAppointment().getAppointmentId() : null,
                entity.getOriginalDoctorId(),
                entity.getProposedDoctorId(),
                entity.getProposedClinicId(),
                entity.getProposedSpecialtyId(),
                entity.getProposedRoomId(),
                entity.getProposedSlotId(),
                entity.getProposedDate(),
                entity.getProposedStartTime(),
                entity.getProposedEndTime(),
                entity.getStatus(),
                entity.getExpiresAt(),
                entity.getRespondedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public List<ReplacementProposalResponse> toResponseList(List<ReplacementProposal> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
