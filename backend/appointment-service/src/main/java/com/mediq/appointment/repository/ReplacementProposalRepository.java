package com.mediq.appointment.repository;

import com.mediq.appointment.entity.ReplacementProposal;
import com.mediq.appointment.entity.ReplacementProposalStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReplacementProposalRepository extends JpaRepository<ReplacementProposal, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ReplacementProposal r WHERE r.proposalId = :proposalId")
    Optional<ReplacementProposal> findByIdWithLock(@Param("proposalId") UUID proposalId);

    List<ReplacementProposal> findByAppointment_AppointmentIdOrderByCreatedAtDesc(UUID appointmentId);

    Optional<ReplacementProposal> findFirstByAppointment_AppointmentIdAndStatus(
            UUID appointmentId,
            ReplacementProposalStatus status
    );

    List<ReplacementProposal> findByStatusAndExpiresAtBefore(
            ReplacementProposalStatus status,
            Instant cutoff
    );
}
