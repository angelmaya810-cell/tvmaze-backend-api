package com.coppel.tvmaze.show.api;

import com.coppel.tvmaze.comment.api.CommentResponse;
import com.coppel.tvmaze.show.domain.ShowDetailResult;
import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ShowDetailResponse {

    private final Map<String, Object> attributes;

    private ShowDetailResponse(Map<String, Object> attributes) {
        this.attributes = Collections.unmodifiableMap(attributes);
    }

    public static ShowDetailResponse from(ShowDetailResult result) {
        Map<String, Object> attributes = new LinkedHashMap<>(result.show().attributes());
        attributes.put(
                "comments",
                result.comments().stream()
                        .map(CommentResponse::from)
                        .toList()
        );
        return new ShowDetailResponse(attributes);
    }

    @JsonAnyGetter
    public Map<String, Object> attributes() {
        return attributes;
    }
}
