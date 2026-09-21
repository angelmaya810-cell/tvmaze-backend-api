package com.coppel.tvmaze.show.api;

import com.coppel.tvmaze.show.domain.ShowSummary;

import java.util.List;

public record ShowSearchResponse(
        long id,
        String name,
        String channel,
        String summary,
        List<String> genres
) {

    public static ShowSearchResponse from(ShowSummary show) {
        return new ShowSearchResponse(
                show.id(),
                show.name(),
                show.channel(),
                show.summary(),
                show.genres()
        );
    }
}
