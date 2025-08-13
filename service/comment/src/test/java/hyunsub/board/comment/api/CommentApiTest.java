package hyunsub.board.comment.api;

import hyunsub.board.comment.service.request.CommentCreateRequest;
import hyunsub.board.comment.service.response.CommentPageResponse;
import hyunsub.board.comment.service.response.CommentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class CommentApiTest {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = createComment(
                new CommentCreateRequest(1L, "my content" , null, 1L));
        CommentResponse response2 = createComment(
                new CommentCreateRequest(1L, "my content2" , response1.getCommentId(), 1L));
        CommentResponse response3 = createComment(
                new CommentCreateRequest(1L, "my content3" , response1.getCommentId(), 1L));

        System.out.println("comment1 Id = " + response1.getCommentId());
        System.out.println("\tcomment2 Id = " + response2.getCommentId());
        System.out.println("\tcomment3 Id = " + response3.getCommentId());
        /*
            comment1 Id = 213194360542662656
                comment2 Id = 213194361419272192
                comment3 Id = 213194361494769664
         */
    }
    
    CommentResponse createComment(CommentCreateRequest request) {
        return restClient.post()
                .uri("/v1/comments")
                .body(request) // 요청 파라미터
                .retrieve() // 해당 uri 호출
                .body(CommentResponse.class); // 응답을 반환 받음
    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
                .uri("/v1/comments/{commentId}", 213194360542662656L)
//                .uri("/v1/comments/{commentId}", 213194361419272192L)
                .retrieve()
                .body(CommentResponse.class);

        System.out.println("response = " + response);
    }

    @Test
    void delete() {
        /*
            comment1 Id = 213194360542662656 (삭제 완료)
                comment2 Id = 213194361419272192
                comment3 Id = 213194361494769664 (삭제 완료)
         */
        restClient.delete()
                .uri("/v1/comments/{commentId}", 213194361419272192L)
                .retrieve()
                .toBodilessEntity(); // retrieve 만 호출하면 서버로 api 호출이 제대로 안되서 추가해줌.
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
                .uri("/v1/comments?articleId=1&page=40000&pageSize=16")
                .retrieve()
                .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for(CommentResponse comment : response.getComments()) {
            if(!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId(), comment.getContent() = " + comment.getCommentId() + ", " + comment.getContent());
        }
    }

    @Test
    void count() {
        CommentResponse commentResponse = createComment(
                new CommentCreateRequest(2L, "my content2" , null, 1L));

        Long count1 = restClient.get()
                .uri("/v1/comments/articles/{articleId}/count", 2L)
                .retrieve()
                .body(Long.class);

        System.out.println("count1 = " + count1);
    }
}
