package kuke.board.common.event;

import kuke.board.common.event.payload.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    /*
        메서드가 아니라 enum의 인스턴스, 상수들이다.
     */
    ARTICLE_CREATED(ArticleCreatedEventPayload.class, Topic.KUKE_BOARD_ARTICLE),
    ARTICLE_UPDATED(ArticleUpdatedEventPayload.class, Topic.KUKE_BOARD_ARTICLE),
    ARTICLE_DELETED(ArticleDeletedEventPayload.class, Topic.KUKE_BOARD_ARTICLE),
    COMMENT_CREATED(CommentCreatedEventPayload.class, Topic.KUKE_BOARD_COMMENT),
    COMMENT_DELETED(CommentDeletedEventPayload.class, Topic.KUKE_BOARD_COMMENT),
    ARTICLE_LIKED(ArticleLikedEventPayload.class, Topic.KUKE_BOARD_LIKE),
    ARTICLE_UNLIKED(ArticleUnlikedEventPayload.class, Topic.KUKE_BOARD_LIKE),
    ARTICLE_VIEWED(ArticleViewedEventPayload.class, Topic.KUKE_BOARD_VIEW)
    ;

    private final Class<? extends EventPayload> payloadClass;
    private final String topic;

    public static EventType from(String type) {
        try {
            /*
                valueOf()는 Java의 모든 enum(열거형) 클래스에 자동으로 생성되는 정적 메서드이다.

                * 예를 들어, EventType.valueOf("ARTICLE_CREATED")는
                ARTICLE_CREATED(ArticleCreatedEventPayload.class, Topic.KUKE_BOARD_ARTICLE)라는
                인스턴스를 찾아 반환
             */
            return valueOf(type);
        } catch (Exception e) {
            log.error("[EventType.from] type={}", type, e);
            return null;
        }
    }

    public static class Topic {
        public static final String KUKE_BOARD_ARTICLE = "kuke-board-article";
        public static final String KUKE_BOARD_COMMENT = "kuke-board-comment";
        public static final String KUKE_BOARD_LIKE = "kuke-board-like";
        public static final String KUKE_BOARD_VIEW = "kuke-board-view";
    }
}
