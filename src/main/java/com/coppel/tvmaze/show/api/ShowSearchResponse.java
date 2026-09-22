package com.coppel.tvmaze.show.api;

import com.coppel.tvmaze.comment.api.CommentResponse;
import com.coppel.tvmaze.show.domain.ShowSearchResult;

import java.util.List;

public record ShowSearchResponse(
        long id,
        String name,
        String channel,
        String summary,
        List<String> genres,
        List<CommentResponse> comments
) {

    public static ShowSearchResponse from(ShowSearchResult result) {
        return new ShowSearchResponse(
                result.show().id(),
                result.show().name(),
                result.show().channel(),
                result.show().summary(),
                result.show().genres(),
                result.comments().stream()
                        .map(CommentResponse::from)
                        .toList()
        );
    }
}
