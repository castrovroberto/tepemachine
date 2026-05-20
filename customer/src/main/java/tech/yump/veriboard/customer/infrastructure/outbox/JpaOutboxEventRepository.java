package tech.yump.veriboard.customer.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    List<OutboxEvent> findTop50ByProcessedFalseOrderByOccurredAtAsc();
}
