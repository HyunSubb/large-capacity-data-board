package hyunsub.board.view.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleViewCountRepository {
    // redis 의존성을 추가하면 기본적으로 RedisTemplate을 사용할 수 있다.
    // 이걸 통해서 Redis와 통신이 가능함.
    /*
        redisTemplate.opsForValue(): String(문자열) 타입 데이터를 조작하는 메서드를 제공합니다.
        get(), set(), increment() 등의 메서드를 사용할 수 있습니다.
     */
    private final StringRedisTemplate redisTemplate;

    public ArticleViewCountRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // view::article::{{article_id}}::view_count
    private static final String KEY_FORMAT = "view::article::%s::view_count";

    public Long read(Long articleId) {
        // get(key): Redis에서 주어진 key에 해당하는 값을 가져옵니다. 값이 없으면 null을 반환합니다.
        String result = redisTemplate.opsForValue().get(generateKey(articleId));
        return result == null ? 0L : Long.parseLong(result);
    }

    public Long increase(Long articleId) {
    // increment(key): 주어진 key의 값을 1 증가시킵니다. 이 작업은 **원자적(atomic)**으로 수행되어,
    // 여러 요청이 동시에 발생해도 안전하게 값을 증가시킬 수 있습니다.
    // redisTemplate.opsForValue().increment() 메서드는
    // 키가 존재하지 않으면 자동으로 키를 생성하고 값을 0으로 초기화한 뒤 1을 증가시켜줍니다.
        return redisTemplate.opsForValue().increment(generateKey(articleId));
    }

    // String.formatted()는 문자열 내의 형식 지정자(%s, %d 등)에 값을 삽입하여 새로운 문자열을 만든다.
    // articleId가 123일 때, KEY_FORMAT.formatted(123)의 결과는 "view::article::123::view_count"가 됨.
    private String generateKey(Long articleId) {
        return KEY_FORMAT.formatted(articleId);
    }
}
