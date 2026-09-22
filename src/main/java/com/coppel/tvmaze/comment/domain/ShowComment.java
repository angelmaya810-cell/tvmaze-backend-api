package com.coppel.tvmaze.comment.domain;

import java.time.Instant;
import java.util.Objects;

public record ShowComment(
        String id,
        long showId,
        String comment,
        int rating,
        Instant createdAt
) {

    public ShowComment {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (showId <= 0) {
            throw new IllegalArgumentException("showId must be greater than zero");
        }
        if (comment == null || comment.isBlank() || comment.length() > 1_000) {
            throw new IllegalArgumentException("comment must contain between 1 and 1000 characters");
        }
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("rating must be between 0 and 5");
        }
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}
