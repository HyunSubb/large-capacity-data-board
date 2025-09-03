package hyunsub.board.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class ArticleCommentCountRepository {
    /*
        이렇게 인기글 서비스에서 데이터를 저장하는 이유는 데이터들을 인기글 서비스의 책임으로 보고
        자체적으로 인기글이 만들어지는 동안 여러 개의 데이터들을 다 가지고 있어야 한다고 했었다.
        만약에 얘가 가지고 있지 않으면 댓글 서비스로 댓글 관련 데이터들을 조회하기 위해서 계속 호출이 필요할 거다.
        다른 서비스로 api 호출을 막기 위해서 인기글에서 자체적으로 데이터를 보관하도록 하는 것.
     */
    private final StringRedisTemplate redisTemplate;

    // hot-article::article::{articleId}::comment-count
    private static final String KEY_FORMAT = "hot-article::article::%s::comment-count";

    public void createOrUpdate(Long articleId, Long commentCount, Duration ttl) {
        // set(key, value, timeout)
        redisTemplate.opsForValue().set(generateKey(articleId), String.valueOf(commentCount), ttl);
    }

    public Long read(Long articleId) {
        String result = redisTemplate.opsForValue().get(generateKey(articleId));
        return result == null ? 0L : Long.valueOf(result);
    }

    private String generateKey(Long articleId) {
        return KEY_FORMAT.formatted(articleId);
    }
}
