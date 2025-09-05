package hyunsub.board.article.service;

import hyunsub.board.article.entity.Article;
import hyunsub.board.article.entity.BoardArticleCount;
import hyunsub.board.article.repository.ArticleRepository;
import hyunsub.board.article.repository.BoardArticleCountRepository;
import hyunsub.board.article.service.request.ArticleCreateRequest;
import hyunsub.board.article.service.request.ArticleUpdateRequest;
import hyunsub.board.article.service.response.ArticlePageResponse;
import hyunsub.board.article.service.response.ArticleResponse;
import hyunsub.board.hyunsub.event.EventType;
import hyunsub.board.hyunsub.event.payload.ArticleCreatedEventPayload;
import hyunsub.board.hyunsub.event.payload.ArticleDeletedEventPayload;
import hyunsub.board.hyunsub.event.payload.ArticleUpdatedEventPayload;
import hyunsub.board.hyunsub.outboxmessagerelay.OutboxEventPublisher;
import hyunsub.board.hyunsub.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final Snowflake snowflake = new Snowflake();
    private final ArticleRepository articleRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final BoardArticleCountRepository boardArticleCountService;

    @Transactional
    public ArticleResponse create(ArticleCreateRequest request) {
        Article article = articleRepository.save(
                Article.create(snowflake.nextId(),
                        request.getTitle(),
                        request.getContent(),
                        request.getBoardId(),
                        request.getWriterId()));
        int result = boardArticleCountService.increase(request.getBoardId());
        if(result == 0) {
            boardArticleCountService.save(
                    BoardArticleCount.init(request.getBoardId(), 1L)
            );
        }

        outboxEventPublisher.publish(
                EventType.ARTICLE_CREATED,
                ArticleCreatedEventPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .boardArticleCount(count(article.getBoardId()))
                        .build(),
                article.getBoardId()
        );

        return ArticleResponse.from(article);
    }

    @Transactional
    public ArticleResponse update(Long articleId, ArticleUpdateRequest request) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        article.update(request.getTitle(), request.getContent());

        outboxEventPublisher.publish(
                EventType.ARTICLE_UPDATED,
                ArticleUpdatedEventPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .build(),
                article.getBoardId()
        );

        return ArticleResponse.from(article);
    }

    public ArticleResponse read(Long articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        return ArticleResponse.from(article);
    }

    @Transactional
    public void delete(Long articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        articleRepository.delete(article);
        boardArticleCountService.decrease(article.getBoardId());

        outboxEventPublisher.publish(
                EventType.ARTICLE_DELETED,
                ArticleDeletedEventPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .boardArticleCount(count(article.getBoardId()))
                        .build(),
                article.getBoardId()
        );
    }

    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {
        return ArticlePageResponse.of(
                // boardId (게시판 번호), (page - 1) * pageSize (offset 계산 공식), pageSize (페이지 당 게시글 개수)
                articleRepository.findAll(boardId, (page - 1) * pageSize, pageSize).stream()
                        .map(ArticleResponse::from)
                        .toList(),
                articleRepository.count(
                        boardId,
                        // 현재 페이지 (page), 페이지 당 게시글 수 (pageSize),
                        // 이동 가능한 페이지의 개수(ex : 1~10 페이지를 이동 가능)
                        // 다음 버튼 유무 확인을 위한 메서드 + 현재 페이지기준으로 가져올 게시글의 수 집계 메서드
                        PageLimitCalculator.calculatePageLimit(page, pageSize, 10L)
                )
        );
    }

    public Long count(Long boardId) {
        return boardArticleCountService.findById(boardId)
                .map(boardArticleCount -> boardArticleCount.getArticleCount())
                .orElse(0L);
    }
}
