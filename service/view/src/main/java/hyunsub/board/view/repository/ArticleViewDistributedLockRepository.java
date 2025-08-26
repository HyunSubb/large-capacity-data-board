package hyunsub.board.view.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class ArticleViewDistributedLockRepository {
    private final StringRedisTemplate redisTemplate;

    public ArticleViewDistributedLockRepository(StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = stringRedisTemplate;
    }

    // view::article::{article_id}::user::{user_id}:lock
    private static final String KEY_FORMAT = "view::article::%s::user::%s::lock";

    public boolean lock(Long articleId, Long userId, Duration ttl) {
        String key = generateKey(articleId, userId);
        // setIfAbsent() 메서드는 Redis에 특정 **키(key)**가 존재하지 않을 때만 값을 설정하는 원자적(atomic) 명령어이다.
        // 이렇게 되면은 데이터 셋팅을 성공한 요청만 정상적으로 True를 응답할 거다.
        // 그래서 True를 받으면은 조회수 증가를 처리할 수 있음을 의미한다.
        return redisTemplate.opsForValue().setIfAbsent(key, "", ttl);
    }

    private String generateKey(Long articleId, Long userId) {
        return KEY_FORMAT.formatted(articleId, userId);
    }
}
