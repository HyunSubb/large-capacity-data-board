package hyunsub.board.like.controller;

import hyunsub.board.like.service.ArticleLikeService;
import hyunsub.board.like.service.response.ArticleLikeResponse;
import org.springframework.web.bind.annotation.*;

@RestController
public class ArticleLikeController {
    private final ArticleLikeService articleLikeService;

    public ArticleLikeController(ArticleLikeService articleLikeService) {
        this.articleLikeService = articleLikeService;
    }

    @GetMapping("/v1/article-likes/articles/{articleId}/users/{userId}")
    public ArticleLikeResponse read(
            @PathVariable Long articleId,
            @PathVariable Long userId
    ) {
        return articleLikeService.read(articleId, userId);
    }

    @GetMapping("/v1/article-likes/articles/{articleId}/count")
    public Long count(
            @PathVariable Long articleId
    ) {
        return articleLikeService.count(articleId);
    }

//    @PostMapping("/v1/article-likes/articles/{articleId}/users/{userId}")
//    public void like(
//            @PathVariable Long articleId,
//            @PathVariable Long userId
//    ) {
//        articleLikeService.like(articleId, userId);
//    }
//
//    @DeleteMapping("/v1/article-likes/articles/{articleId}/users/{userId}")
//    public void unLike(
//            @PathVariable Long articleId,
//            @PathVariable Long userId
//    ) {
//        articleLikeService.unLike(articleId, userId);
//    }

    @PostMapping("/v1/article-likes/articles/{articleId}/users/{userId}/pessimistic-lock-1")
    public void likePessimisticLock1(
            @PathVariable Long articleId,
            @PathVariable Long userId
    ) {
        articleLikeService.likePessimistickLock1(articleId, userId);
    }

    @DeleteMapping("/v1/article-likes/articles/{articleId}/users/{userId}/pessimistic-lock-1")
    public void unLikePessimisticLock1(
            @PathVariable Long articleId,
            @PathVariable Long userId
    ) {
        articleLikeService.unlikePessimistickLock1(articleId, userId);
    }

    @PostMapping("/v1/article-likes/articles/{articleId}/users/{userId}/pessimistic-lock-2")
    public void likePessimisticLock2(
            @PathVariable Long articleId,
            @PathVariable Long userId
    ) {
        articleLikeService.likePessimistickLock2(articleId, userId);
    }

    @DeleteMapping("/v1/article-likes/articles/{articleId}/users/{userId}/pessimistic-lock-2")
    public void unLikePessimisticLock2(
            @PathVariable Long articleId,
            @PathVariable Long userId
    ) {
        articleLikeService.unlikePessimistickLock2(articleId, userId);
    }

    @PostMapping("/v1/article-likes/articles/{articleId}/users/{userId}/optimistic-lock")
    public void likeOptimisticLock(
            @PathVariable Long articleId,
            @PathVariable Long userId
    ) {
        articleLikeService.likeOptimistickLock(articleId, userId);
    }

    @DeleteMapping("/v1/article-likes/articles/{articleId}/users/{userId}/optimistic-lock")
    public void unLikeOptimisticLock(
            @PathVariable Long articleId,
            @PathVariable Long userId
    ) {
        articleLikeService.unlikeOptimistickLock(articleId, userId);
    }
}
