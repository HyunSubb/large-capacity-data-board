package hyunsub.board.hyunsub.outboxmessagerelay;

import hyunsub.board.hyunsub.event.Event;
import hyunsub.board.hyunsub.event.EventPayload;
import hyunsub.board.hyunsub.event.EventType;
import hyunsub.board.hyunsub.snowflake.Snowflake;
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

        // 이 코드가 실행되면 OutboxEventPublisher는 OutboxEvent라는 이벤트를 Spring 컨테이너에 "나 지금 이 이벤트를 발행했어!"라고 알린다.
        // 이 시점에서 OutboxEventPublisher는 이 이벤트를 누가 처리할지 전혀 알 필요가 없다.

        // MessageRelay.createOutbox(OutboxEvent outboxEvent) 메서드에서는
        // @TransactionalEventListener라는 어노테이션이 붙어 있다.
        // 이 어노테이션은 Spring에게 "만약 OutboxEvent 타입의 이벤트가 발행되면, 이 createOutbox 메서드를 실행해줘"라고 미리 약속해 놓은 것과 같습니다.

        // applicationEventPublisher.publishEvent() 메서드에 전달한 객체(파라미터)가
        // @TransactionalEventListener 어노테이션이 붙은 메서드(MessageRelay에 있음)의 파라미터로 그대로 전달되어 처리됩니다.
        applicationEventPublisher.publishEvent(OutboxEvent.of(outbox));
    }
}
