package hyunsub.board.hotarticle.service.eventhandler;

import hyunsub.board.hotarticle.repository.ArticleCreatedTimeRepository;
import hyunsub.board.hotarticle.utils.TimeCalculatorUtils;
import hyunsub.board.hyunsub.event.Event;
import hyunsub.board.hyunsub.event.EventType;
import hyunsub.board.hyunsub.event.payload.ArticleCreatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleCreatedEventHandler implements EventHandler<ArticleCreatedEventPayload>{
    private final ArticleCreatedTimeRepository articleCreatedTimeRepository;

    @Override
    public void handle(Event<ArticleCreatedEventPayload> event) {
        ArticleCreatedEventPayload payload = event.getPayload();
        articleCreatedTimeRepository.createOrUpdate(
                payload.getArticleId(),
                payload.getCreatedAt(),
                TimeCalculatorUtils.calculateDurationToMidnight() // 오늘 자정까지만 저장
        );
    }

    @Override
    public boolean supports(Event<ArticleCreatedEventPayload> event) {
        return EventType.ARTICLE_CREATED == event.getType();
    }

    @Override
    public Long findArticleId(Event<ArticleCreatedEventPayload> event) {
        return event.getPayload().getArticleId();
    }
}
