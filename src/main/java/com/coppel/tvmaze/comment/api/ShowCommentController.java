package com.coppel.tvmaze.comment.api;

import com.coppel.tvmaze.comment.application.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shows")
public class ShowCommentController {

    private final CommentService commentService;

    public ShowCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{showId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateCommentResponse create(
            @PathVariable @Positive(message = "showId must be greater than zero") long showId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return CreateCommentResponse.from(
                commentService.create(showId, request.comment(), request.rating())
        );
    }
}
