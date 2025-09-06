package hyunsub.board.articleread.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewClient {
    private RestClient restClient;
    @Value("${endpoints.kuke-board-view-service.url}")
    private String viewServiceUrl;

    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(viewServiceUrl);
    }

    /*
        동작 흐름: article-read 서비스가 게시글의 조회수를 요청하면, ViewClient.count() 메서드가 실행됩니다.

        먼저 Redis에서 articleViewCount 캐시의 articleId 키를 확인합니다.

        만약 데이터가 있으면, Redis에 저장된 조회수를 즉시 반환합니다. 이 경우, 실제 view-service API 호출은 발생하지 않습니다.

        만약 데이터가 없으면, try-catch 블록 안의 코드가 실행되어 view-service API를 호출하고 조회수를 가져옵니다. * 이 값을 Redis에 1초 동안 캐싱한 후 반환합니다. *

        만약 데이터가 없다면, view-service로 API 요청을 보내서 조회수를 가져오고,
        조회수 값이 반환된다면 스프링은 그 값을 가로채서 Redis에 우리가 설정한 캐시의 이름을 key(articleViewCount::{articleId}) 로 하여 저장을 한다.
        이러한 매커니즘으로 인해, 개발자는 캐시 저장 로직을 직접 구현하지 않고도 Cacheable 어노테션 하나만으로 조회 성능을 최적화할 수 있다.
     */
    /*
        @Cacheable: 이 어노테이션은 메서드가 호출될 때 캐시를 먼저 확인하도록 지시한다.
        key = "#articleId": Redis에서 데이터를 저장하고 찾을 때 사용할 키(key)를 지정합니다. 여기서는 게시글의 ID를 사용한다. / articleViewCount::{articleId} 형태로 redis에 저장된다.
        value = "articleViewCount": CacheConfig에서 정의한 캐시의 이름을 지정한다.
     */
    @Cacheable(key = "#articleId", value = "articleViewCount")
    public long count(Long articleId) {
        log.info("[ViewClient.count] articleId={}", articleId);
        try {
            return restClient.get()
                    .uri("/v1/article-views/articles/{articleId}/count", articleId)
                    .retrieve()
                    .body(Long.class);
        } catch (Exception e) {
            log.error("[ViewClient.count] articleId={}", articleId, e);
            return 0;
        }
    }

}
