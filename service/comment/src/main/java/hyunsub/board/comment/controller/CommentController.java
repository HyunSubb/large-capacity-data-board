package hyunsub.board.comment.controller;

import hyunsub.board.comment.service.CommentService;
import hyunsub.board.comment.service.request.CommentCreateRequest;
import hyunsub.board.comment.service.response.CommentPageResponse;
import hyunsub.board.comment.service.response.CommentResponse;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/v1/comments/{commentId}")
    public CommentResponse read(
            @PathVariable("commentId") Long commentId
    ) {
       return commentService.read(commentId);
    }

    @PostMapping("/v1/comments")
    public CommentResponse create(
            @RequestBody CommentCreateRequest request
    ) {
        return commentService.create(request);
    }

    @DeleteMapping("/v1/comments/{commentId}")
    public void delete(@PathVariable("commentId") Long commentId) {
        commentService.delete(commentId);
    }

    @GetMapping("/v1/comments")
    public CommentPageResponse readAll(
            @RequestParam("articleId") Long articleId,
            @RequestParam("page") Long page,
            @RequestParam("pageSize") Long pageSize
    ) {
        return commentService.readAll(articleId, page, pageSize);
    }
}
