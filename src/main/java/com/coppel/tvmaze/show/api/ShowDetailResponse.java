package com.coppel.tvmaze.show.api;

import com.coppel.tvmaze.show.domain.ShowDetails;
import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.Map;

public final class ShowDetailResponse {

    private final Map<String, Object> attributes;

    private ShowDetailResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public static ShowDetailResponse from(ShowDetails show) {
        return new ShowDetailResponse(show.attributes());
    }

    @JsonAnyGetter
    public Map<String, Object> attributes() {
        return attributes;
    }
}
