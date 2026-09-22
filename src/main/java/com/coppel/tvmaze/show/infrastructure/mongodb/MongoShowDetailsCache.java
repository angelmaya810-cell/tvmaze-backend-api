package com.coppel.tvmaze.show.infrastructure.mongodb;

import com.coppel.tvmaze.show.application.port.out.ShowDetailsCache;
import com.coppel.tvmaze.show.domain.ShowDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Component
public class MongoShowDetailsCache implements ShowDetailsCache {

    private final CachedShowMongoRepository repository;
    private final Clock clock;

    @Autowired
    public MongoShowDetailsCache(CachedShowMongoRepository repository) {
        this(repository, Clock.systemUTC());
    }

    MongoShowDetailsCache(CachedShowMongoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public Optional<ShowDetails> findById(long showId) {
        return repository.findById(showId)
                .map(document -> new ShowDetails(document.id(), document.show()));
    }

    @Override
    public void save(ShowDetails show) {
        repository.save(new CachedShowDocument(
                show.id(),
                show.attributes(),
                Instant.now(clock)
        ));
    }
}
