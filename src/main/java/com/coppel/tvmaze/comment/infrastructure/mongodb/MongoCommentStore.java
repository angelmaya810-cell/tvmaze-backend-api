package com.coppel.tvmaze.comment.infrastructure.mongodb;

import com.coppel.tvmaze.comment.application.port.out.CommentStore;
import com.coppel.tvmaze.comment.domain.ShowComment;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@Component
public class MongoCommentStore implements CommentStore {

    private final CommentMongoRepository repository;
    private final Clock clock;
    private final Supplier<String> idSupplier;

    @Autowired
    public MongoCommentStore(CommentMongoRepository repository) {
        this(repository, Clock.systemUTC(), () -> new ObjectId().toHexString());
    }

    MongoCommentStore(
            CommentMongoRepository repository,
            Clock clock,
            Supplier<String> idSupplier
    ) {
        this.repository = repository;
        this.clock = clock;
        this.idSupplier = idSupplier;
    }

    @Override
    public ShowComment save(long showId, String comment, int rating) {
        CommentDocument saved = repository.save(new CommentDocument(
                idSupplier.get(),
                showId,
                comment,
                rating,
                Instant.now(clock)
        ));
        return toDomain(saved);
    }

    @Override
    public List<ShowComment> findByShowIds(Collection<Long> showIds) {
        if (showIds.isEmpty()) {
            return List.of();
        }

        return repository.findAllByShowIdInOrderByShowIdAscCreatedAtAsc(showIds).stream()
                .map(this::toDomain)
                .toList();
    }

    private ShowComment toDomain(CommentDocument document) {
        return new ShowComment(
                document.id(),
                document.showId(),
                document.comment(),
                document.rating(),
                document.createdAt()
        );
    }
}
