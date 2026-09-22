package com.coppel.tvmaze.comment.application;

import com.coppel.tvmaze.comment.application.port.out.CommentStore;
import com.coppel.tvmaze.comment.domain.ShowComment;
import com.coppel.tvmaze.show.application.InvalidShowIdException;
import com.coppel.tvmaze.show.application.ShowDetailService;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private static final int MAX_COMMENT_LENGTH = 1_000;

    private final ShowDetailService showDetailService;
    private final CommentStore commentStore;

    public CommentService(
            ShowDetailService showDetailService,
            CommentStore commentStore
    ) {
        this.showDetailService = showDetailService;
        this.commentStore = commentStore;
    }

    public ShowComment create(long showId, String comment, Integer rating) {
        if (showId <= 0) {
            throw new InvalidShowIdException();
        }

        String normalizedComment = normalizeComment(comment);
        validateRating(rating);
        showDetailService.findById(showId);
        return commentStore.save(showId, normalizedComment, rating);
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) {
            throw new InvalidCommentException("comment must not be blank");
        }

        String normalizedComment = comment.strip();
        if (normalizedComment.length() > MAX_COMMENT_LENGTH) {
            throw new InvalidCommentException("comment must not exceed 1000 characters");
        }
        return normalizedComment;
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 0 || rating > 5) {
            throw new InvalidCommentException("rating must be between 0 and 5");
        }
    }
}
