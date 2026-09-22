package com.coppel.tvmaze.comment.infrastructure.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Objects;

@Document(collection = "comments")
@CompoundIndex(
        name = "show_id_created_at_idx",
        def = "{'show_id': 1, 'created_at': 1}"
)
public record CommentDocument(
        @Id String id,
        @Field("show_id") long showId,
        String comment,
        int rating,
        @Field("created_at") Instant createdAt
) {

    public CommentDocument {
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
