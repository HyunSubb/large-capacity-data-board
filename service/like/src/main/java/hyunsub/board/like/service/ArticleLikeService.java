package hyunsub.board.like.service;

import hyunsub.board.like.entity.ArticleLike;
import hyunsub.board.like.entity.ArticleLikeCount;
import hyunsub.board.like.repository.ArticleLikeCountRepository;
import hyunsub.board.like.repository.ArticleLikeRepository;
import hyunsub.board.like.service.response.ArticleLikeResponse;
import hyunsub.board.hyunsub.snowflake.Snowflake;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleLikeService {
    private final Snowflake snowflake = new Snowflake();
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleLikeCountRepository articleLikeCountRepository;

    public ArticleLikeService(ArticleLikeRepository articleLikeRepository, ArticleLikeCountRepository articleLikeCountRepository) {
        this.articleLikeRepository = articleLikeRepository;
        this.articleLikeCountRepository = articleLikeCountRepository;
    }

    public ArticleLikeResponse read(Long articleId, Long userId) {
        return articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
//              .map(ArticleLikeResponse::from)
                .map(articleLike -> ArticleLikeResponse.from(articleLike))
                .orElseThrow();
    }

//    @Transactional
//    public void like(Long articleId, Long userId) {
//        articleLikeRepository.save(
//                ArticleLike.create(snowflake.nextId(), articleId, userId));
//    }
//
//    @Transactional
//    public void unLike(Long articleId, Long userId) {
//        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
//                .ifPresent(articleLike -> articleLikeRepository.delete(articleLike));
//    }

    /*
     * update ... set .. where
     */
    @Transactional
    public void likePessimistickLock1(Long articleId, Long userId) {
        articleLikeRepository.save(
                ArticleLike.create(snowflake.nextId(), articleId, userId));

        int result = articleLikeCountRepository.increase(articleId);
        if(result == 0) {
            // 최초 요청 시 update 되는 레코드가 없으므로, 1로 초기화
            // 트래픽이 순식간에 몰릴 수 있는 상황에는 유실될 수 있으므로, 게시글 생성 시점에 미리 0으로 초기화 해줄 수도 있다.
            articleLikeCountRepository.save(
                    ArticleLikeCount.init(articleId, 1L)
            );
        }
    }

    /*
     * update ... set .. where
     */
    @Transactional
    public void unlikePessimistickLock1(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    articleLikeRepository.delete(articleLike);
                    articleLikeCountRepository.decrease(articleId);
                });
    }

    /*
     * select ... for update + update
     */
    @Transactional
    public void likePessimistickLock2(Long articleId, Long userId) {
        articleLikeRepository.save(
                ArticleLike.create(snowflake.nextId(), articleId, userId));

        // orElseGet은 Optional 객체가 비어있을 때 (값이 없을 때)만 특정 코드를 실행하여 값을 생성하고 반환하는 메서드
        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId).orElseGet(
                () -> ArticleLikeCount.init(articleId, 0L));

        articleLikeCount.increase();
        articleLikeCountRepository.save(articleLikeCount);
    }

    /*
     * select ... for update + update
     */
    @Transactional
    public void unlikePessimistickLock2(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    articleLikeRepository.delete(articleLike);
                    ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId).orElseThrow();
                    articleLikeCount.decrease();
                });
    }

    @Transactional
    public void likeOptimistickLock(Long articleId, Long userId) {
        articleLikeRepository.save(
                ArticleLike.create(snowflake.nextId(), articleId, userId));

        ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(articleId)
                .orElseGet(() -> ArticleLikeCount.init(articleId, 0L));
        articleLikeCount.increase();
        articleLikeCountRepository.save(articleLikeCount);
    }

    @Transactional
    public void unlikeOptimistickLock(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    articleLikeRepository.delete(articleLike);
                    ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(articleId).orElseThrow();
                    articleLikeCount.decrease();
                });
    }

    public Long count(Long articleId) {
        return articleLikeCountRepository.findById(articleId)
                .map(articleLikeCount -> articleLikeCount.getLikeCount())
                .orElse(0L);
    }

}
