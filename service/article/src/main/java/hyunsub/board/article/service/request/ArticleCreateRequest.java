package hyunsub.board.article.service.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ArticleCreateRequest {
    private String title;
    private String content;
    private Long writerId;
    private Long boardId;
}
