package hyunsub.board.comment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Table(name = "comment")
@Getter
@Entity
@ToString
@NoArgsConstructor
public class Comment {
    @Id
    private Long commentId;
    private String content;
    private Long parentCommentId;
    private Long articleId; // shard key
    private Long writerId;
    private Boolean deleted;
    private LocalDateTime createdAt;

    public static Comment create(Long commentId, String content, Long parentCommentId, Long articleId, Long writerId) {
        Comment comment = new Comment();
        comment.commentId = commentId;
        comment.content = content;
        comment.parentCommentId = parentCommentId == null ? commentId : parentCommentId;
        comment.articleId = articleId;
        comment.writerId = writerId;
        comment.deleted = false; // 댓글 삭제 여부
        comment.createdAt = LocalDateTime.now();
        return comment;
    }

    // 내가 1 depth 댓글이면 true
    public boolean isRoot() {
        return parentCommentId.longValue() == commentId;
    }

    // 해당 댓글을 삭제했다면 true로 변경
    public void delete() {
        deleted = true;
    }
}
