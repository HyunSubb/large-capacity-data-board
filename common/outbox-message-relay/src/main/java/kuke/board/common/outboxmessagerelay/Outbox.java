package kuke.board.common.outboxmessagerelay;

import jakarta.persistence.*;
import kuke.board.common.event.EventType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/*
    이 클래스는 데이터베이스의 outbox 테이블과 매핑되는 JPA 엔티티입니다. 카프카로 전송해야 할 메시지 정보를 임시로 저장하는 역할을 합니다.
 */
// `outbox` 테이블과 매핑되는 엔티티
@Table(name = "outbox")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbox {
    // outbox_id 필드를 기본 키로 사용합니다.
    @Id
    private Long outboxId;

    // EnumType.STRING으로 설정하여 EventType enum의 이름을 문자열로 DB에 저장합니다.
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    // 이벤트 페이로드(내용)를 JSON 문자열로 저장합니다.
    private String payload;

    // 샤드 키. 이 값을 기준으로 여러 인스턴스에 메시지 처리 책임을 분산합니다.
    private Long shardKey;

    // 메시지가 생성된 시간
    private LocalDateTime createdAt;

    // 아웃박스 엔티티를 생성하는 정적 팩토리 메서드
    public static Outbox create(Long outboxId, EventType eventType, String payload, Long shardKey) {
        Outbox outbox = new Outbox();
        outbox.outboxId = outboxId;
        outbox.eventType = eventType;
        outbox.payload = payload;
        outbox.shardKey = shardKey;
        outbox.createdAt = LocalDateTime.now();
        return outbox;
    }
}
