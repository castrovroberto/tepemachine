package tech.yump.veriboard.customer.infrastructure.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPoller {

    private final JpaOutboxEventRepository outboxEventRepository;
    private final OutboxEventPublishingHelper publishingHelper;

    @Scheduled(fixedDelay = 5000)
    public void pollAndPublish() {
        List<OutboxEvent> pending = outboxEventRepository.findTop50ByProcessedFalseOrderByOccurredAtAsc();
        if (pending.isEmpty()) {
            return;
        }

        log.debug("Outbox poller: processing {} pending event(s)", pending.size());

        for (OutboxEvent event : pending) {
            publishingHelper.publishAndMark(event);
        }
    }
}
