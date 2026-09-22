package com.coppel.tvmaze.comment.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank(message = "comment must not be blank")
        @Size(max = 1_000, message = "comment must not exceed 1000 characters")
        String comment,

        @NotNull(message = "rating is required")
        @Min(value = 0, message = "rating must be between 0 and 5")
        @Max(value = 5, message = "rating must be between 0 and 5")
        Integer rating
) {
}
