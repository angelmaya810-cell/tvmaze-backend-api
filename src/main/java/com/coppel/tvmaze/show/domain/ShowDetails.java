package com.coppel.tvmaze.show.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ShowDetails(long id, Map<String, Object> attributes) {

    public ShowDetails {
        attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }
}
