package com.mediq.appointment.service;

import com.mediq.appointment.client.SchedulingClient;
import com.mediq.appointment.dto.response.ReplacementProposalResponse;
import com.mediq.appointment.entity.Appointment;
import com.mediq.appointment.entity.AppointmentStatus;
import com.mediq.appointment.entity.ReplacementProposal;
import com.mediq.appointment.entity.ReplacementProposalStatus;
import com.mediq.appointment.event.EventPublisher;
import com.mediq.appointment.exception.BusinessException;
import com.mediq.appointment.exception.ErrorCode;
import com.mediq.appointment.mapper.ReplacementProposalMapper;
import com.mediq.appointment.repository.AppointmentHistoryRepository;
import com.mediq.appointment.repository.AppointmentRepository;
import com.mediq.appointment.repository.ReplacementProposalRepository;
import com.mediq.appointment.validation.ReplacementProposalValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplacementProposalConcurrencyTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentHistoryRepository appointmentHistoryRepository;

    @Mock
    private ReplacementProposalRepository replacementProposalRepository;

    @Mock
    private SchedulingClient schedulingClient;

    @Mock
    private EventPublisher eventPublisher;

    private ReplacementProposalServiceImpl proposalService;

    @BeforeEach
    void setUp() {
        ReplacementProposalValidator validator = new ReplacementProposalValidator();
        ReplacementProposalMapper mapper = new ReplacementProposalMapper();

        proposalService = new ReplacementProposalServiceImpl(
                appointmentRepository,
                appointmentHistoryRepository,
                replacementProposalRepository,
                schedulingClient,
                validator,
                mapper,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Concurrent accept on same ReplacementProposal: Only 1 succeeds, 9 fail with INVALID_PROPOSAL_STATUS_TRANSITION")
    void concurrentAcceptProposal_OnlyOneSucceeds() throws Exception {
        UUID proposalId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID proposedSlotId = UUID.randomUUID();
        UUID oldSlotId = UUID.randomUUID();
        UUID proposedDoctorId = UUID.randomUUID();
        UUID proposedClinicId = UUID.randomUUID();
        UUID proposedSpecialtyId = UUID.randomUUID();
        UUID proposedRoomId = UUID.randomUUID();

        Appointment appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .doctorId(UUID.randomUUID())
                .clinicId(UUID.randomUUID())
                .roomId(UUID.randomUUID())
                .slotId(oldSlotId)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        ReplacementProposal proposal = ReplacementProposal.builder()
                .proposalId(proposalId)
                .appointment(appointment)
                .originalDoctorId(appointment.getDoctorId())
                .proposedDoctorId(proposedDoctorId)
                .proposedClinicId(proposedClinicId)
                .proposedSpecialtyId(proposedSpecialtyId)
                .proposedRoomId(proposedRoomId)
                .proposedSlotId(proposedSlotId)
                .proposedDate(LocalDate.now().plusDays(1))
                .proposedStartTime(LocalTime.of(9, 0))
                .proposedEndTime(LocalTime.of(9, 30))
                .status(ReplacementProposalStatus.PENDING)
                .expiresAt(Instant.now().plus(12, ChronoUnit.HOURS))
                .build();

        SchedulingClient.SlotSnapshotDto slotSnapshot = new SchedulingClient.SlotSnapshotDto(
                proposedSlotId,
                UUID.randomUUID(),
                proposedDoctorId,
                proposedClinicId,
                proposedSpecialtyId,
                proposedRoomId,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(9, 30),
                "AVAILABLE"
        );

        when(schedulingClient.getSlot(proposedSlotId)).thenReturn(Optional.of(slotSnapshot));
        when(schedulingClient.holdSlot(proposedSlotId)).thenReturn(true);
        when(schedulingClient.bookSlot(proposedSlotId)).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));
        when(replacementProposalRepository.save(any(ReplacementProposal.class))).thenAnswer(i -> i.getArgument(0));

        // Simulate DB PESSIMISTIC_WRITE lock behavior
        ReentrantLock dbLock = new ReentrantLock();
        when(replacementProposalRepository.findByIdWithLock(proposalId)).thenAnswer(invocation -> {
            dbLock.lock();
            return Optional.of(proposal);
        });

        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger invalidStatusFailures = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    proposalService.acceptProposal(proposalId);
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    if (e.getErrorCode() == ErrorCode.INVALID_PROPOSAL_STATUS_TRANSITION) {
                        invalidStatusFailures.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    if (dbLock.isHeldByCurrentThread()) {
                        dbLock.unlock();
                    }
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        finishLatch.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(invalidStatusFailures.get()).isEqualTo(9);
        assertThat(proposal.getStatus()).isEqualTo(ReplacementProposalStatus.ACCEPTED);
    }
}
