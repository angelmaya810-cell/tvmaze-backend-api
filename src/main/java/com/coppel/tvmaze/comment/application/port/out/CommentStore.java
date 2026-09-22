package com.coppel.tvmaze.comment.application.port.out;

import com.coppel.tvmaze.comment.domain.ShowComment;

import java.util.Collection;
import java.util.List;

public interface CommentStore {

    ShowComment save(long showId, String comment, int rating);

    List<ShowComment> findByShowIds(Collection<Long> showIds);
}
