package hyunsub.board.hotarticle.client;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleClient {
    private RestClient restClient;

    @Value("${endpoints.kuke-board-article-service.url}")
    private String articleServiceUrl;

    /*
        @PostConstruct가 붙은 메서드는 Spring 컨테이너가 ArticleClient 객체를 생성하고 @Autowired나
        @RequiredArgsConstructor를 통해 의존성 주입(이 경우 articleServiceUrl)까지 마친 후에 자동으로 호출된다.

        1. private String articleServiceUrl 필드에 application.yml에 있는 http://127.0.0.1:9000 값이 주입이 된다.

        2. initRestClient() 메서드는 이 주입이 완료된 후에 호출되어,
        articleServiceUrl 값을 사용해 RestClient 객체를 초기화하는 역할을 합니다.
     */
    @PostConstruct
    void initRestClient() {
        restClient = RestClient.create(articleServiceUrl);
    }

    // article Service에 게시글 호출
    public ArticleResponse read(Long articleId) {
        try {
            return restClient.get()
                    .uri("/v1/articles/{articleId}", articleId)
                    .retrieve()
                    .body(ArticleResponse.class);
        } catch (Exception e) {
            log.error("[ArticleClient.read] articleId={}", articleId, e);
        }
        return null;
    }


    @Getter
    public static class ArticleResponse {
        private Long articleId;
        private String title;
        private LocalDateTime createdAt;
    }
}
