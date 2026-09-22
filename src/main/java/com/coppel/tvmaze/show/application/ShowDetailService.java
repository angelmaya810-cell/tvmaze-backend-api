package com.coppel.tvmaze.show.application;

import com.coppel.tvmaze.show.application.port.out.ShowDetailsClient;
import com.coppel.tvmaze.show.application.port.out.ShowDetailsCache;
import com.coppel.tvmaze.show.domain.ShowDetails;
import org.springframework.stereotype.Service;

@Service
public class ShowDetailService {

    private final ShowDetailsClient showDetailsClient;
    private final ShowDetailsCache showDetailsCache;

    public ShowDetailService(
            ShowDetailsClient showDetailsClient,
            ShowDetailsCache showDetailsCache
    ) {
        this.showDetailsClient = showDetailsClient;
        this.showDetailsCache = showDetailsCache;
    }

    public ShowDetails findById(long showId) {
        if (showId <= 0) {
            throw new InvalidShowIdException();
        }

        return showDetailsCache.findById(showId)
                .orElseGet(() -> findAndCache(showId));
    }

    private ShowDetails findAndCache(long showId) {
        ShowDetails show = showDetailsClient.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(showId));
        showDetailsCache.save(show);
        return show;
    }
}
