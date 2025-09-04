package kuke.board.common.outboxmessagerelay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
    이 클래스는 트랜잭셔널 아웃박스 패턴의 핵심 로직을 담고 있습니다. 이벤트 저장, 카프카 전송, 그리고 실패 시 재전송 로직을 담당합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRelay {
    private final OutboxRepository outboxRepository;
    private final MessageRelayCoordinator messageRelayCoordinator;
    private final KafkaTemplate<String, String> messageRelayKafkaTemplate;

    // 트랜잭션이 커밋되기 전에 아웃박스 이벤트를 받아서 저장
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createOutbox(OutboxEvent outboxEvent) {
        log.info("[MessageRelay.createOutbox] outboxEvent={}", outboxEvent);
        // 비즈니스 로직의 트랜잭션 내에서 Outbox 엔티티를 저장하여 데이터 일관성을 보장
        outboxRepository.save(outboxEvent.getOutbox());
    }

    // 트랜잭션이 커밋된 후 카프카에 이벤트를 전송 (비동기)
    @Async("messageRelayPublishEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishEvent(OutboxEvent outboxEvent) {
        publishEvent(outboxEvent.getOutbox());
    }

    private void publishEvent(Outbox outbox) {
        try {
            // 카프카로 메시지를 보내고, 1초 안에 전송이 완료될 때까지 기다립니다.
            // messageRelayKafkaTemplate.send(topic, key, data)
            messageRelayKafkaTemplate.send(
                    outbox.getEventType().getTopic(),
                    String.valueOf(outbox.getShardKey()),
                    outbox.getPayload()
            ).get(1, TimeUnit.SECONDS);
            // 전송 성공 시, 아웃박스 테이블에서 메시지 삭제
            outboxRepository.delete(outbox);
        } catch (Exception e) {
            // 전송 실패 시, 에러를 로깅하고 메시지는 DB에 남겨둡니다.
            log.error("[MessageRelay.publishEvent] outbox={}", outbox, e);
        }
    }

    // 10초마다 주기적으로 미전송 이벤트들을 재전송
    @Scheduled(
            fixedDelay = 10,
            initialDelay = 5,
            timeUnit = TimeUnit.SECONDS,
            scheduler = "messageRelayPublishPendingEventExecutor"
    )
    public void publishPendingEvent() {
        // MessageRelayCoordinator에게 할당된 샤드 목록을 가져옵니다.
        AssignedShard assignedShard = messageRelayCoordinator.assignShards();
        log.info("[MessageRelay.publishPendingEvent] assignedShard size={}", assignedShard.getShards().size());
        // 할당된 샤드에 대해 DB에서 10초 이상 남아있는 메시지를 조회하여 재전송을 시도합니다.
        for (Long shard : assignedShard.getShards()) {
            List<Outbox> outboxes = outboxRepository.findAllByShardKeyAndCreatedAtLessThanEqualOrderByCreatedAtAsc(
                    shard,
                    LocalDateTime.now().minusSeconds(10), // 생성된 지 10초 지난 이벤트
                    Pageable.ofSize(100)
            );
            for (Outbox outbox : outboxes) {
                publishEvent(outbox);
            }
        }
    }
}
