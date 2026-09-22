package com.coppel.tvmaze.show.domain;

import com.coppel.tvmaze.comment.domain.ShowComment;

import java.util.List;
import java.util.Objects;

public record ShowSearchResult(
        ShowSummary show,
        List<ShowComment> comments
) {

    public ShowSearchResult {
        Objects.requireNonNull(show, "show must not be null");
        comments = comments == null ? List.of() : List.copyOf(comments);
    }
}
