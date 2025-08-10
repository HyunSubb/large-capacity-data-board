package hyunsub.board.article.api;

import hyunsub.board.article.service.request.ArticleCreateRequest;
import hyunsub.board.article.service.request.ArticleUpdateRequest;
import hyunsub.board.article.service.response.ArticlePageResponse;
import hyunsub.board.article.service.response.ArticleResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class ArticleApiTest {
    // RestClient는 Spring Boot 3 이상에서 나온 Rest 템플릿과 같은 Http요청을 할 수 있는 클래스이다.
    RestClient restClient = RestClient.create("http://localhost:9000");

    @Test
    void createTest() {
        ArticleResponse response = create(new ArticleCreateRequest(
                "hi", "my content", 1L, 1L));

        System.out.println("response = " + response);
    }

    ArticleResponse create(ArticleCreateRequest request) {
        return restClient.post()
                .uri("/v1/articles")
                .body(request)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void readTest() {
        ArticleResponse response = read(212088390588092416L);
        System.out.println("response = " + response);
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
                .uri("/v1/articles/{articleId}", articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void updateTest() {
        ArticleResponse response = update(212088390588092416L, new ArticleUpdateRequest(
                "new hi", "my new content"));
        System.out.println("response = " + response);
    }

    ArticleResponse update(Long articleId, ArticleUpdateRequest request) {
        return restClient.put()
                .uri("/v1/articles/{articleId}", articleId)
                .body(request)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void deleteTest() {
        delete(212085765897506816L);
    }

    ArticleResponse delete(Long articleId) {
        return restClient.delete()
                .uri("/v1/articles/{articleId}", articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void readAllTest() {
        ArticlePageResponse response = restClient.get()
                // 1번 게시판에서 1페이지에 30개의 게시글을 조회
//                .uri("/v1/articles?boardId=1&page=1&pageSize=30")
                .uri("/v1/articles?boardId=1&page=50000&pageSize=30")
                .retrieve()
                .body(ArticlePageResponse.class);

        System.out.println("response = " + response);
        for(ArticleResponse article : response.getArticles()) {
            System.out.println("article = " + article);
        }
    }
}
