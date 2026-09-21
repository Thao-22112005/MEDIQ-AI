package com.mediq.appointment.service;

import com.mediq.appointment.dto.request.CreateReplacementProposalRequest;
import com.mediq.appointment.dto.request.RescheduleProposalRequest;
import com.mediq.appointment.dto.response.ReplacementProposalResponse;

import java.util.List;
import java.util.UUID;

public interface ReplacementProposalService {

    ReplacementProposalResponse createProposal(UUID appointmentId, CreateReplacementProposalRequest request);

    ReplacementProposalResponse getProposalById(UUID appointmentId, UUID proposalId);

    List<ReplacementProposalResponse> listProposalsByAppointment(UUID appointmentId);

    ReplacementProposalResponse acceptProposal(UUID proposalId);

    ReplacementProposalResponse rescheduleProposal(UUID proposalId, RescheduleProposalRequest request);

    ReplacementProposalResponse cancelProposal(UUID proposalId);

    int expirePendingProposals();
}


