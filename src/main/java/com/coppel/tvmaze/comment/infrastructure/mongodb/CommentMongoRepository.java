package com.coppel.tvmaze.comment.infrastructure.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentMongoRepository extends MongoRepository<CommentDocument, String> {
}
