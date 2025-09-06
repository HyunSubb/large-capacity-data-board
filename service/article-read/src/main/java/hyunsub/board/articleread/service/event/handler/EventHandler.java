package hyunsub.board.articleread.service.event.handler;


import hyunsub.board.hyunsub.event.Event;
import hyunsub.board.hyunsub.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    /*
        이벤트를 처리하는 핸들 메서드
     */
    void handle(Event<T> event);
    /*
        인터페이스 구현체가 이 이벤트를 지원하는지 확인하기 위한 메서드
     */
    boolean supports(Event<T> event);
}
