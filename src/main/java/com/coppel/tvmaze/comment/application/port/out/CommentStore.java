package com.coppel.tvmaze.comment.application.port.out;

import com.coppel.tvmaze.comment.domain.ShowComment;

public interface CommentStore {

    ShowComment save(long showId, String comment, int rating);
}
