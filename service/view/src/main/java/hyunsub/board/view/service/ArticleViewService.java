package hyunsub.board.view.service;

import hyunsub.board.view.repository.ArticleViewCountRepository;
import hyunsub.board.view.repository.ArticleViewDistributedLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ArticleViewService {
    private final ArticleViewDistributedLockRepository articleViewDistributedLockRepository;
    private final ArticleViewCountBackUpProcessor articleViewCountBackUpProcessor;
    private final ArticleViewCountRepository articleViewCountRepository;
    // 백업 단위
    private static final int BACK_UP_BACH_SIZE = 1000;
    private static final Duration TTL = Duration.ofMinutes(10);

    public Long increase(Long articleId, Long userId) {

        if(!articleViewDistributedLockRepository.lock(articleId, userId, TTL)) {
            // 락 획득을 먼저 시도하고 획득에 실패했으면 증가를 처리 안하고 그대로 현재 조회수 반환
            return articleViewCountRepository.read(articleId);
        }

        Long count = articleViewCountRepository.increase(articleId);
        if(count % BACK_UP_BACH_SIZE == 0) {
            articleViewCountBackUpProcessor.backUp(articleId, count);
        }
        return count;
    }

    public Long count(Long articleId) {
        return articleViewCountRepository.read(articleId);
    }
}
