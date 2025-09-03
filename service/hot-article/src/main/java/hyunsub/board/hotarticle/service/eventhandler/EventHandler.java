package hyunsub.board.hotarticle.service.eventhandler;

import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    /*
        이벤트를 받았을 때 처리되는 로직을 정의하는 핸들 메서드
     */
    void handle(Event<T> event);
    /*
        이벤트 핸들러 구현체가 이 이벤트를 지원하는지 확인하는 서포트 메서드
     */
    boolean supports(Event<T> event);
    /*
        이 이벤트가 어떤 아티클에 대한 건지 아티클 아이디를 찾아주는 메서드
     */
    Long findArticleId(Event<T> event);
}
