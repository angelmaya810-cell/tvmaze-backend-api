package com.coppel.tvmaze.show.infrastructure.tvmaze;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record TvMazeShow(
        Long id,
        String name,
        TvMazeChannel network,
        TvMazeChannel webChannel,
        String summary,
        List<String> genres
) {
}
