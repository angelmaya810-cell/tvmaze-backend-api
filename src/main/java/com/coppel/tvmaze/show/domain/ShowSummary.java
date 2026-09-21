package com.coppel.tvmaze.show.domain;

import java.util.List;

public record ShowSummary(
        long id,
        String name,
        String channel,
        String summary,
        List<String> genres
) {

    public ShowSummary {
        genres = genres == null ? List.of() : List.copyOf(genres);
    }
}
