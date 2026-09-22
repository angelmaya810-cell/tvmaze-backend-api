package com.coppel.tvmaze.show.application.port.out;

import com.coppel.tvmaze.show.domain.ShowDetails;

import java.util.Optional;

public interface ShowDetailsClient {

    Optional<ShowDetails> findById(long showId);
}
