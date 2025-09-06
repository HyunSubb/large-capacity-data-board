package hyunsub.board.articleread.service.event.handler;

import hyunsub.board.articleread.repository.ArticleIdListRepository;
import hyunsub.board.articleread.repository.ArticleQueryModelRepository;
import hyunsub.board.articleread.repository.BoardArticleCountRepository;
import hyunsub.board.hyunsub.event.Event;
import hyunsub.board.hyunsub.event.EventType;
import hyunsub.board.hyunsub.event.payload.ArticleDeletedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleDeletedEventHandler implements EventHandler<ArticleDeletedEventPayload> {
    private final ArticleIdListRepository articleIdListRepository;
    private final ArticleQueryModelRepository articleQueryModelRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;

    @Override
    public void handle(Event<ArticleDeletedEventPayload> event) {
        ArticleDeletedEventPayload payload = event.getPayload();
        // 이거 순서도 중요한 게 목록을 먼저 삭제해야 한다.
        // QueryModel 부터 먼저 삭제 해버리면 List에는 남아 있어서 클릭했는데 조회가 안되는 경우가 발생할 수 있기 때문임.
        articleIdListRepository.delete(payload.getBoardId(), payload.getArticleId());
        articleQueryModelRepository.delete(payload.getArticleId());
        boardArticleCountRepository.createOrUpdate(payload.getBoardId(), payload.getBoardArticleCount());
    }

    @Override
    public boolean supports(Event<ArticleDeletedEventPayload> event) {
        return EventType.ARTICLE_DELETED == event.getType();
    }
}
