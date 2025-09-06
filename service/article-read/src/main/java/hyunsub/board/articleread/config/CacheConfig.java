package hyunsub.board.articleread.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Map;

@Configuration // 이 클래스가 Spring의 설정 클래스임을 나타냄
@EnableCaching // Spring 애플리케이션 내에서 캐싱 기능을 활성화한다. 이 어노테이션이 있어야 @Cacheable 같은 캐시 관련 어노테이션이 정상적으로 동작한다.
public class CacheConfig {
    // cacheManager 메서드는 RedisCacheManager라는 캐시 관리자를 생성하는 역할을 한다.
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .withInitialCacheConfigurations( // withInitialCacheConfigurations()는 초기 캐시 설정들을 정의하는 부분
                        Map.of(
                                // 여기에서 **articleViewCount**라는 이름의 캐시를 정의함.
                                // 이것은 ViewClient에서 value = "articleViewCount"로 지정하여 사용되는 캐시의 이름과 동일하다.
                                // entryTtl(Duration.ofSeconds(1)): 이 캐시에 저장되는 데이터의 유효 기간(Time-To-Live)을 1초로 설정했습니다.
                                // 즉, 게시글 조회수 데이터는 Redis에 저장된 후 1초가 지나면 자동으로 만료됩니다.
                                "articleViewCount", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(1))
                        )
                )
                .build();
    }
}
