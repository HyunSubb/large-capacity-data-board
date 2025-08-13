package hyunsub.board.view.service;

import hyunsub.board.view.entity.ArticleViewCount;
import hyunsub.board.view.repository.ArticleViewCountBackUpRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ArticleViewCountBackUpProcessor {
    private final ArticleViewCountBackUpRepository articleViewCountBackUpRepository;

    public ArticleViewCountBackUpProcessor(ArticleViewCountBackUpRepository articleViewCountBackUpRepository) {
        this.articleViewCountBackUpRepository = articleViewCountBackUpRepository;
    }

    @Transactional
    public void backUp(Long articleId, Long viewCount) {
        int result = articleViewCountBackUpRepository.updateViewCount(articleId, viewCount);
        if (result == 0) {
            articleViewCountBackUpRepository.findById(articleId)
                    .ifPresentOrElse(ignored -> { },
                        () -> articleViewCountBackUpRepository.save(
                            ArticleViewCount.init(articleId, viewCount))
                    );
        }
    }
}
