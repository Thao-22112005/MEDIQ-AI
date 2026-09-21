package com.mediq.appointment.controller;

import com.mediq.appointment.dto.request.CreateReplacementProposalRequest;
import com.mediq.appointment.dto.request.RescheduleProposalRequest;
import com.mediq.appointment.dto.response.ReplacementProposalResponse;
import com.mediq.appointment.service.ReplacementProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReplacementProposalController {

    private final ReplacementProposalService replacementProposalService;

    @PostMapping("/appointments/{appointmentId}/replacement-proposals")
    public ResponseEntity<ReplacementProposalResponse> createProposal(
            @PathVariable("appointmentId") UUID appointmentId,
            @RequestBody CreateReplacementProposalRequest request
    ) {
        ReplacementProposalResponse response = replacementProposalService.createProposal(appointmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/appointments/{appointmentId}/replacement-proposals/{proposalId}")
    public ResponseEntity<ReplacementProposalResponse> getProposalById(
            @PathVariable("appointmentId") UUID appointmentId,
            @PathVariable("proposalId") UUID proposalId
    ) {
        return ResponseEntity.ok(replacementProposalService.getProposalById(appointmentId, proposalId));
    }

    @GetMapping("/appointments/{appointmentId}/replacement-proposals")
    public ResponseEntity<List<ReplacementProposalResponse>> listProposalsByAppointment(
            @PathVariable("appointmentId") UUID appointmentId
    ) {
        return ResponseEntity.ok(replacementProposalService.listProposalsByAppointment(appointmentId));
    }

    @PostMapping("/replacement-proposals/{proposalId}/accept")
    public ResponseEntity<ReplacementProposalResponse> acceptProposal(
            @PathVariable("proposalId") UUID proposalId
    ) {
        return ResponseEntity.ok(replacementProposalService.acceptProposal(proposalId));
    }

    @PostMapping("/replacement-proposals/{proposalId}/reschedule")
    public ResponseEntity<ReplacementProposalResponse> rescheduleProposal(
            @PathVariable("proposalId") UUID proposalId,
            @RequestBody RescheduleProposalRequest request
    ) {
        return ResponseEntity.ok(replacementProposalService.rescheduleProposal(proposalId, request));
    }

    @PostMapping("/replacement-proposals/{proposalId}/cancel")
    public ResponseEntity<ReplacementProposalResponse> cancelProposal(
            @PathVariable("proposalId") UUID proposalId
    ) {
        return ResponseEntity.ok(replacementProposalService.cancelProposal(proposalId));
    }

    @PostMapping("/replacement-proposals/expire-check")
    public ResponseEntity<Map<String, Object>> expirePendingProposals() {
        int expiredCount = replacementProposalService.expirePendingProposals();
        return ResponseEntity.ok(Map.of("expiredCount", expiredCount));
    }
}


