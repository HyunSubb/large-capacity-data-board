package hyunsub.board.comment.service;

import hyunsub.board.comment.entity.Comment;
import hyunsub.board.comment.repository.CommentRepository;
import hyunsub.board.comment.service.request.CommentCreateRequest;
import hyunsub.board.comment.service.response.CommentResponse;
import kuke.board.common.snowflake.Snowflake;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.function.Predicate.not;

@Service
public class CommentService {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    // 생성
    @Transactional
    public CommentResponse create(CommentCreateRequest request) {
        Comment parent = findParent(request);
        Comment comment = commentRepository.save(
                Comment.create(
                        snowflake.nextId(),
                        request.getContent(),
                        parent == null ? null : parent.getCommentId(),
                        request.getArticleId(),
                        request.getWriterId()
                )
        );
        return CommentResponse.from(comment);
    }

    private Comment findParent(CommentCreateRequest request) {
        Long parentCommentId = request.getParentCommentId();
        if (parentCommentId == null) {
            return null;
        }
        return commentRepository.findById(parentCommentId)
//                .filter(not(Comment::getDeleted)) <- 메서드 레퍼런스
                .filter(comment -> !comment.getDeleted())
//                .filter(Comment::isRoot) <- 메서드 레퍼런스
                .filter(comment -> comment.isRoot())
                .orElseThrow();
    }

    // 조회
    public CommentResponse read(Long commentId) {
        return CommentResponse.from(
                commentRepository.findById(commentId).orElseThrow()
        );
    }
    
    // 삭제
    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
//                .filter(not(Comment::getDeleted))
                .filter(comment -> !comment.getDeleted())
                .ifPresent(comment -> {
                    if (hasChildren(comment)) {
                        comment.delete();
                    } else {
                        delete(comment);
                    }
                });
    }

    private boolean hasChildren(Comment comment) {
        // 자식 댓글이 있는지 확인을 위함
        return commentRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2;
    }

    private void delete(Comment comment) {
        commentRepository.delete(comment);
        if (!comment.isRoot()) {
            commentRepository.findById(comment.getParentCommentId())
//                    .filter(Comment::getDeleted) // 부모 댓글이 삭제가 되어졌는 지 확인
                    .filter(parentComment -> parentComment.getDeleted())
//                    .filter(not(this::hasChildren)) // 부모 댓글이 아직 자식을 가지고 있는 지 확인
                    .filter(parentComment -> !hasChildren(parentComment))
//                    .ifPresent(this::delete); // 부모 댓글이 존재하면 부모 댓글 삭제
                    .ifPresent(parentComment -> delete(parentComment));
        }
    }

}
