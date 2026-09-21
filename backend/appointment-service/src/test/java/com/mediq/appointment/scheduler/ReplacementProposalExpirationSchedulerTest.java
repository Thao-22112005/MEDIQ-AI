package com.mediq.appointment.scheduler;

import com.mediq.appointment.service.ReplacementProposalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplacementProposalExpirationSchedulerTest {

    @Mock
    private ReplacementProposalService replacementProposalService;

    @InjectMocks
    private ReplacementProposalExpirationScheduler scheduler;

    @Test
    @DisplayName("Should invoke expirePendingProposals on schedule and log when count > 0")
    void shouldInvokeExpirePendingProposalsWhenExpiredFound() {
        when(replacementProposalService.expirePendingProposals()).thenReturn(3);

        scheduler.scanAndExpireProposals();

        verify(replacementProposalService).expirePendingProposals();
    }

    @Test
    @DisplayName("Should invoke expirePendingProposals on schedule when no expired proposals found")
    void shouldInvokeExpirePendingProposalsWhenNoneFound() {
        when(replacementProposalService.expirePendingProposals()).thenReturn(0);

        scheduler.scanAndExpireProposals();

        verify(replacementProposalService).expirePendingProposals();
    }
}
