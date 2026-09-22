package com.coppel.tvmaze.comment.api;

import com.coppel.tvmaze.comment.domain.ShowComment;

public record CreateCommentResponse(
        String status,
        String commentId,
        long showId
) {

    public static CreateCommentResponse from(ShowComment comment) {
        return new CreateCommentResponse("CREATED", comment.id(), comment.showId());
    }
}
