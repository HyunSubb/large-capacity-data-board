package kuke.board.common.outboxmessagerelay;

import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;
import kuke.board.common.event.EventType;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/*
    이 클래스는 다른 서비스 모듈에서 비즈니스 로직 처리 후 이벤트를 발행할 때 사용하는 퍼블리셔입니다.
 */
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {
    // 고유 ID 생성을 위한 Snowflake 객체들
    private final Snowflake outboxIdSnowflake = new Snowflake();
    private final Snowflake eventIdSnowflake = new Snowflake();

    // Spring의 로컬 이벤트 발행자
    private final ApplicationEventPublisher applicationEventPublisher;

    // 이벤트를 발행하는 핵심 메서드
    public void publish(EventType type, EventPayload payload, Long shardKey) {
        // 1. Outbox 엔티티 생성:
        //    - outboxIdSnowflake.nextId(): Snowflake를 이용해 고유 ID를 생성합니다.
        //    - Event.of(...).toJson(): 이벤트 페이로드를 JSON 문자열로 변환합니다.
        //    - shardKey % MessageRelayConstants.SHARD_COUNT: 샤드 키를 이용해
        //      전체 샤드 개수(4)에 대한 나머지 연산으로 샤드를 결정합니다.
        Outbox outbox = Outbox.create(
                outboxIdSnowflake.nextId(),
                type,
                Event.of(
                        eventIdSnowflake.nextId(), type, payload
                ).toJson(),
                shardKey % MessageRelayConstants.SHARD_COUNT
        );

        // 2. Spring의 로컬 이벤트로 OutboxEvent를 발행합니다.
        //    이 이벤트는 MessageRelay의 createOutbox 메서드에 의해 처리됩니다.
        applicationEventPublisher.publishEvent(OutboxEvent.of(outbox));
    }
}
