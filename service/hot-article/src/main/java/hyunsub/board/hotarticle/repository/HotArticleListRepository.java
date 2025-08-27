package hyunsub.board.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HotArticleListRepository {
    private final StringRedisTemplate redisTemplate;

    // hot-article::list::{yyyyMMdd}
    private static final String KEY_FORMAT = "hot-article::list::%s";

    // 시간 정보를 문자열 변환을 하기위한 데이터 타임 포매터 / yyyyMMdd 이런식으로..
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /*
        articleId : 게시글 아이디
        time : 게시글 작성 시간
        score : 게시글 점수 = 댓글 수, 조회 수, 좋아요 수 조합해서 계산
        limit : 매일 상위 10건의 인기글만 저장. 아마 10이 전달될 거다.
        ttl : 시간 지나면 삭제되도록 만료기간
        redisTemplate.executePipelined() : 이거를 통하면 Redis와 네트워크 통신으로 한 번만 연결하면서 여러개 연산 수행 가능

        Zset (Sorted Set)은 **점수(score)**를 기준으로 정렬된 **고유한 멤버(member)**들의 집합
        z 가 붙은 메서드들이 Zset와 관련된 메서드들이다.

        conn.zAdd(key, score, String.valueOf(articleId)); 이 코드는 다음과 같은 의미를 가집니다:

        key: Zset의 이름입니다.

        score: articleId의 점수입니다. Zset은 이 점수를 기준으로 멤버를 정렬합니다.

        String.valueOf(articleId): Zset에 저장될 멤버입니다. articleId는 Long 타입이므로, 문자열로 변환하여 저장합니다.

        conn.zRemRange는 Redis의 Zset(Sorted Set)에서 지정된 범위의 멤버들을 제거하는 역할을 한다. -> 상위 10개 데이터만 남도록
     */
    public void add(Long articleId, LocalDateTime time, Long score, Long limit, Duration ttl) {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey(time);
            conn.zAdd(key, score, String.valueOf(articleId));
            conn.zRemRange(key, 0, - limit - 1);
            conn.expire(key, ttl.toSeconds());
            return null;
        });
    }

    public void remove(Long articleId, LocalDateTime time) {
        redisTemplate.opsForZSet().remove(generateKey(time), String.valueOf(articleId));
    }

    private String generateKey(LocalDateTime time) {
        return generateKey(TIME_FORMATTER.format(time));
    }

    private String generateKey(String dateStr) {
        return KEY_FORMAT.formatted(dateStr);
    }

    /*
        reverseRangeWithScores(generateKey(dateStr), 0, -1): Zset에서 데이터를 가져오는 핵심 명령어입니다.

        reverseRange: Zset은 기본적으로 점수가 낮은 순서대로 정렬되지만, 이 메서드는 점수가 높은 순서대로(역순으로) 멤버들을 가져옵니다.

        WithScores: 멤버와 함께 점수도 같이 가져오도록 지정합니다.

        generateKey(dateStr): 데이터를 가져올 Zset의 키를 동적으로 생성합니다. 이 키는 아마 hot-article::list::{yyyyMMdd} 형식일 것입니다.

        0: 시작 인덱스를 0으로 지정하여 Zset의 첫 번째 요소부터 가져오겠다는 의미입니다.

        -1: 끝 인덱스를 -1로 지정하여 Zset의 마지막 요소까지 가져오겠다는 의미입니다. 즉, Zset의 모든 멤버를 가져옵니다.

        .stream(): Set을 스트림으로 변환하여, 연속적인 데이터를 파이프라인으로 처리할 수 있게 합니다.

        .peek(): 중간 연산자로, 스트림의 각 요소에 대한 디버깅이나 로깅 목적으로 사용됩니다.
        여기서는 log.info를 통해 각 게시글(articleId)과 점수(score)를 출력하여 데이터를 확인하는 용도로 쓰입니다.
        peek은 스트림의 요소를 소비하지 않고 다음 단계로 그대로 전달합니다.

        .map(ZSetOperations.TypedTuple::getValue): 스트림의 각 요소를 변환합니다.
        ZSetOperations.TypedTuple 객체에서 getValue() 메서드를 호출하여 **게시글 ID(value)**만 추출합니다.

        .map(Long::valueOf): 이전에 추출한 문자열 형태의 게시글 ID를 Long 타입으로 다시 변환합니다.

        .toList(): 스트림의 모든 처리 과정을 마친 후, 최종 결과를 List 형태로 수집하여 반환합니다.
     */
    public List<Long> readAll(String dateStr) {
        return redisTemplate.opsForZSet()
                .reverseRangeWithScores(generateKey(dateStr), 0, -1).stream()
                .peek(tuple ->
                        log.info("[HotArticleListRepository.readAll] articleId={}, score={}", tuple.getValue(), tuple.getScore()))
                .map(ZSetOperations.TypedTuple::getValue)
                .map(Long::valueOf)
                .toList();
    }
}
