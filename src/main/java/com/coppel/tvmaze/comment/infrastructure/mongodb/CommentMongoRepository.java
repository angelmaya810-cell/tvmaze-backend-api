package com.coppel.tvmaze.comment.infrastructure.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface CommentMongoRepository extends MongoRepository<CommentDocument, String> {

    List<CommentDocument> findAllByShowIdInOrderByShowIdAscCreatedAtAsc(
            Collection<Long> showIds
    );
}
