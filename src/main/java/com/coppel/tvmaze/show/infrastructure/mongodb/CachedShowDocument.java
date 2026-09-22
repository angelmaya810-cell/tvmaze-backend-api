package com.coppel.tvmaze.show.infrastructure.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Document(collection = "shows_cache")
public record CachedShowDocument(
        @Id long id,
        Map<String, Object> show,
        @Field("cached_at") Instant cachedAt
) {

    public CachedShowDocument {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be greater than zero");
        }
        Objects.requireNonNull(show, "show must not be null");
        Objects.requireNonNull(cachedAt, "cachedAt must not be null");
        show = Collections.unmodifiableMap(new LinkedHashMap<>(show));
    }
}
