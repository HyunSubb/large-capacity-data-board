package hyunsub.board.hotarticle.consumer;

import hyunsub.board.hotarticle.service.HotArticleService;
import hyunsub.board.hyunsub.event.Event;
import hyunsub.board.hyunsub.event.EventPayload;
import hyunsub.board.hyunsub.event.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotArticleEventConsumer {
    private final HotArticleService hotArticleService;

    /*
        KafkaListener를 통해서 topic을 구독해서 이벤트를 처리할 수 있다.
     */
    @KafkaListener(topics = {
            EventType.Topic.KUKE_BOARD_ARTICLE,
            EventType.Topic.KUKE_BOARD_COMMENT,
            EventType.Topic.KUKE_BOARD_LIKE,
            EventType.Topic.KUKE_BOARD_VIEW
    })
    public void listen(String message, Acknowledgment ack) {
        log.info("[HotArticleEventConsumer.listen] received message={}", message);
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            hotArticleService.handleEvent(event);
        }
        ack.acknowledge();
    }
}
