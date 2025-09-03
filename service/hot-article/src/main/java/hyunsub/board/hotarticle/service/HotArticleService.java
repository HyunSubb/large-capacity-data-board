package hyunsub.board.hotarticle.service;

import hyunsub.board.hotarticle.client.ArticleClient;
import hyunsub.board.hotarticle.repository.HotArticleListRepository;
import hyunsub.board.hotarticle.service.eventhandler.EventHandler;
import hyunsub.board.hotarticle.service.response.HotArticleResponse;
import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;
import kuke.board.common.event.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotArticleService {
    /*
        이벤트를 통해서 인기글 점수를 계산하고 hotArticleListRepository에 인기글 아이디를 저장하는 그런 로직을 만들어 볼 거다.

        인기글 서비스는 컨슈머 어플리케이션임.

        HotArticleListRepository (레디스) 에서는 게시글의 id만 저장하고 있음.
        원본 게시글의 데이터는 ArticleClient를 통해서 Article 서비스 모듈에서 가져올 거다.
     */
    private final ArticleClient articleClient;
    /*
        이렇게 List로 주입을 하게 되면 이벤트 핸들러 구현체들이 다 같이 주입이 된다.
     */
    private final List<EventHandler> eventHandlers;
    private final HotArticleScoreUpdater hotArticleScoreUpdater;
    /*
        인기글 조회할 때 사용
     */
    private final HotArticleListRepository hotArticleListRepository;

    public void handleEvent(Event<EventPayload> event) {
        EventHandler<EventPayload> eventHandler = findEventHandler(event);
        if (eventHandler == null) {
            return;
        }

        /*
            이 이벤트가 게시글 생성 또는 삭제된 이벤트인지 검사를 할 거다.
         */
        if (isArticleCreatedOrDeleted(event)) {
            eventHandler.handle(event);
        } else {
            // 생성 또는 삭제 이벤트가 아니라면 점수를 업데이트하는 메서드를 실행
            hotArticleScoreUpdater.update(event, eventHandler);
        }
    }

    private EventHandler<EventPayload> findEventHandler(Event<EventPayload> event) {
        /*
            스트림 돌면서 이벤트 핸들러가 전달받은 이벤트를 지원하는 건지 필터링으로 검증해주고 찾았으면 응답해주면 된다.
         */
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .orElse(null);
    }

    private boolean isArticleCreatedOrDeleted(Event<EventPayload> event) {
        return EventType.ARTICLE_CREATED == event.getType() || EventType.ARTICLE_DELETED == event.getType();
    }

    /*
        파라미터로 yyyyMMdd를 받게된다.
     */
    public List<HotArticleResponse> readAll(String dateStr) {
        return hotArticleListRepository.readAll(dateStr).stream()
                .map(articleClient::read)
                .filter(Objects::nonNull)
                .map(HotArticleResponse::from)
                .toList();
    }
}
