package com.coppel.tvmaze.show.infrastructure.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CachedShowMongoRepository
        extends MongoRepository<CachedShowDocument, Long> {
}
