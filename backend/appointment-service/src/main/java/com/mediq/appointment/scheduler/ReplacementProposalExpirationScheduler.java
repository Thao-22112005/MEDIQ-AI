package com.mediq.appointment.scheduler;

import com.mediq.appointment.service.ReplacementProposalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReplacementProposalExpirationScheduler {

    private final ReplacementProposalService replacementProposalService;

    @Scheduled(cron = "${mediq.scheduling.replacement-proposal-expiry-cron:0 */5 * * * *}")
    public void scanAndExpireProposals() {
        int expiredCount = replacementProposalService.expirePendingProposals();
        if (expiredCount > 0) {
            log.info("ReplacementProposalExpirationScheduler: Expired {} pending replacement proposal(s) exceeding 24h window (BR-REPLACEMENT-004)", expiredCount);
        }
    }
}
