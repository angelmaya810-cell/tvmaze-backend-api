package com.coppel.tvmaze.show.application;

import com.coppel.tvmaze.show.application.port.out.ShowDetailsClient;
import com.coppel.tvmaze.show.domain.ShowDetails;
import org.springframework.stereotype.Service;

@Service
public class ShowDetailService {

    private final ShowDetailsClient showDetailsClient;

    public ShowDetailService(ShowDetailsClient showDetailsClient) {
        this.showDetailsClient = showDetailsClient;
    }

    public ShowDetails findById(long showId) {
        if (showId <= 0) {
            throw new InvalidShowIdException();
        }

        return showDetailsClient.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(showId));
    }
}
