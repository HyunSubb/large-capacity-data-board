package kuke.board.common.outboxmessagerelay;

import lombok.Getter;
import lombok.ToString;

/*
    이 클래스는 Spring의 ApplicationEventPublisher를 통해 로컬에서 전달되는 이벤트 객체입니다.
    Outbox 엔티티를 감싸서 이벤트 핸들러(MessageRelay)로 전달하는 역할을 합니다.
 */

@Getter
@ToString
public class OutboxEvent {
    // 이 이벤트에 담겨 전달될 Outbox 객체
    private Outbox outbox;

    // Outbox 객체를 인자로 받아 OutboxEvent 객체를 생성하는 정적 팩토리 메서드
    public static OutboxEvent of(Outbox outbox) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.outbox = outbox;
        return outboxEvent;
    }
}
