package kuke.board.common.outboxmessagerelay;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/*
    다중 인스턴스 환경에서 각 인스턴스에 샤드 처리 권한을 부여하고, 서버의 상태를 관리하는 역할을 합니다.

    인스턴스는 말하자면 각각의 서버 ex) 포트9000번을 A,B,C 서버에서 실행하면 그 각각을 앱 인스턴스라고 부른다.
    그리고 이렇게 각각 다른 서버에서 하나의 포트를 실행하려면 운영 환경에서 서버의 부담이 커질 때 서버를 늘려주는 로드밸런싱 같은
    기법을 사용할 때 샤드 분배 기능들이 필요하다.

    MessageRelayCoordinator는 여러 대의 서버가 하나의 작업을 동시에 처리할 때
    서로 충돌하지 않고 자기 할당량만 책임지도록 역할을 분배해주는 조율자입니다.
    이를 통해 효율성을 높이고, 메시지가 중복으로 전송되는 문제를 원천적으로 방지합니다.
    
    - 살아있는 애플리케이션들을 추적하고 관리하는 클래스
 */
@Component
@RequiredArgsConstructor
public class MessageRelayCoordinator {
    private final StringRedisTemplate redisTemplate;

    // application.yml에 정의된 애플리케이션 이름
    @Value("${spring.application.name}")
    private String applicationName;

    // UUID로 생성된 고유한 애플리케이션 ID / 현재 실행중인 애플리케이션은 uuid로 생성
    private final String APP_ID = UUID.randomUUID().toString();

    // 핑을 3초마다 Redis에 보냅니다.
    private final int PING_INTERVAL_SECONDS = 3;
    // 3번의 핑 실패(총 9초) 시 인스턴스가 죽었다고 판단합니다.
    private final int PING_FAILURE_THRESHOLD = 3;

    // 현재 인스턴스에 할당된 샤드 목록을 계산하여 반환합니다.
    public AssignedShard assignShards() {
        return AssignedShard.of(APP_ID, findAppIds(), MessageRelayConstants.SHARD_COUNT);
    }

    private List<String> findAppIds() {
        // Redis의 Sorted Set을 이용해 점수(최근 시간)가 높은 순서대로 모든 ID를 가져옵니다.
        return redisTemplate.opsForZSet().reverseRange(generateKey(), 0, -1).stream()
                .sorted()
                .toList();
    }

    // Coordinator는 자신을 실행한 애플리케이션의 식별자와 현재 시간으로, 중앙 저장소에 3초 간격으로 ping을 보낸다.
    // 이를 통해 Coordinator는 실행 중인 애플리케이션 목록을 파악하고, 각 애플리케이션에 샤드를 적절히 분산한다.
    // 중앙 저장소(Redis)는 마지막 ping을 받은 지 9초가 지났으면 애플리케이션이 종료되었다고 판단하고 목록에서 제거한다.
    @Scheduled(fixedDelay = PING_INTERVAL_SECONDS, timeUnit = TimeUnit.SECONDS)
    public void ping() {
        // Pipeline을 사용해 여러 Redis 명령어를 한 번에 보냅니다.
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey();
            // 현재 시간으로 인스턴스 ID를 업데이트합니다.
            conn.zAdd(key, Instant.now().toEpochMilli(), APP_ID);
            // 9초(3초 * 3)가 지난 인스턴스들은 제거합니다.
            conn.zRemRangeByScore(
                    key,
                    Double.NEGATIVE_INFINITY,
                    // 9초가 지난 애플리케이션들은 제거
                    Instant.now().minusSeconds(PING_INTERVAL_SECONDS * PING_FAILURE_THRESHOLD).toEpochMilli()
            );
            return null;
        });
    }

    // 애플리케이션이 종료될 때 Redis에서 자신의 ID를 제거합니다.
    @PreDestroy
    public void leave() {
        redisTemplate.opsForZSet().remove(generateKey(), APP_ID);
    }

    private String generateKey() {
        // 애플리케이션 이름에 기반한 Redis 키를 생성합니다.
        return "message-relay-coordinator::app-list::%s".formatted(applicationName);
    }
}
