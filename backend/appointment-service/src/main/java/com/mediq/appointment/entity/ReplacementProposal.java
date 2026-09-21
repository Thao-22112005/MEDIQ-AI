package com.mediq.appointment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "replacement_proposals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplacementProposal {

    @Id
    @Column(name = "proposal_id")
    private UUID proposalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(name = "original_doctor_id")
    private UUID originalDoctorId;

    @Column(name = "proposed_doctor_id")
    private UUID proposedDoctorId;

    @Column(name = "proposed_clinic_id")
    private UUID proposedClinicId;

    @Column(name = "proposed_specialty_id")
    private UUID proposedSpecialtyId;

    @Column(name = "proposed_room_id")
    private UUID proposedRoomId;

    @Column(name = "proposed_slot_id")
    private UUID proposedSlotId;

    @Column(name = "proposed_date")
    private LocalDate proposedDate;

    @Column(name = "proposed_start_time")
    private LocalTime proposedStartTime;

    @Column(name = "proposed_end_time")
    private LocalTime proposedEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReplacementProposalStatus status;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.proposalId == null) {
            this.proposalId = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = this.createdAt;
        }
        if (this.status == null) {
            this.status = ReplacementProposalStatus.PENDING;
        }
        // BR-REPLACEMENT-004: Patient has 24 hours to respond
        if (this.expiresAt == null) {
            this.expiresAt = this.createdAt.plus(24, ChronoUnit.HOURS);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
