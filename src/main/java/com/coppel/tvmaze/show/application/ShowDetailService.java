package com.coppel.tvmaze.show.application;

import com.coppel.tvmaze.comment.application.port.out.CommentStore;
import com.coppel.tvmaze.show.application.port.out.ShowDetailsClient;
import com.coppel.tvmaze.show.application.port.out.ShowDetailsCache;
import com.coppel.tvmaze.show.domain.ShowDetailResult;
import com.coppel.tvmaze.show.domain.ShowDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowDetailService {

    private final ShowDetailsClient showDetailsClient;
    private final ShowDetailsCache showDetailsCache;
    private final CommentStore commentStore;

    public ShowDetailService(
            ShowDetailsClient showDetailsClient,
            ShowDetailsCache showDetailsCache,
            CommentStore commentStore
    ) {
        this.showDetailsClient = showDetailsClient;
        this.showDetailsCache = showDetailsCache;
        this.commentStore = commentStore;
    }

    public ShowDetails findById(long showId) {
        if (showId <= 0) {
            throw new InvalidShowIdException();
        }

        return showDetailsCache.findById(showId)
                .orElseGet(() -> findAndCache(showId));
    }

    public ShowDetailResult findWithCommentsById(long showId) {
        ShowDetails show = findById(showId);
        return new ShowDetailResult(
                show,
                commentStore.findByShowIds(List.of(showId))
        );
    }

    private ShowDetails findAndCache(long showId) {
        ShowDetails show = showDetailsClient.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(showId));
        showDetailsCache.save(show);
        return show;
    }
}
