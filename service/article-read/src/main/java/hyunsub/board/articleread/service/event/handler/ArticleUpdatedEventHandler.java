package hyunsub.board.articleread.service.event.handler;

import hyunsub.board.articleread.repository.ArticleQueryModelRepository;
import hyunsub.board.hyunsub.event.Event;
import hyunsub.board.hyunsub.event.EventType;
import hyunsub.board.hyunsub.event.payload.ArticleUpdatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleUpdatedEventHandler implements EventHandler<ArticleUpdatedEventPayload> {
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleUpdatedEventPayload> event) {
        articleQueryModelRepository.read(event.getPayload().getArticleId())
                .ifPresent(articleQueryModel -> {
                    articleQueryModel.updateBy(event.getPayload());
                    articleQueryModelRepository.update(articleQueryModel);
                });
    }

    @Override
    public boolean supports(Event<ArticleUpdatedEventPayload> event) {
        return EventType.ARTICLE_UPDATED == event.getType();
    }
}
