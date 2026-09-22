package com.coppel.tvmaze.comment.api;

import com.coppel.tvmaze.comment.domain.ShowComment;

public record CommentResponse(
        String comment,
        int rating
) {

    public static CommentResponse from(ShowComment comment) {
        return new CommentResponse(comment.comment(), comment.rating());
    }
}
