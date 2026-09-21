package com.coppel.tvmaze.show.infrastructure.tvmaze;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record TvMazeChannel(String name) {
}
