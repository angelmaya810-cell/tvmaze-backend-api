package com.coppel.tvmaze.comment.infrastructure.mongodb;

import com.coppel.tvmaze.comment.domain.ShowComment;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MongoCommentStoreTest {

    private static final Instant CREATED_AT = Instant.parse("2026-09-22T12:00:00Z");
    private static final String COMMENT_ID = "68d17b9510b2ac45f2931234";

    @Test
    void savesAndReturnsThePersistedComment() {
        CommentMongoRepository repository = mock(CommentMongoRepository.class);
        when(repository.save(any(CommentDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        MongoCommentStore store = new MongoCommentStore(
                repository,
                Clock.fixed(CREATED_AT, ZoneOffset.UTC),
                () -> COMMENT_ID
        );

        ShowComment result = store.save(1L, "Great show", 5);

        CommentDocument expected = new CommentDocument(
                COMMENT_ID,
                1L,
                "Great show",
                5,
                CREATED_AT
        );
        verify(repository).save(expected);
        assertThat(result).isEqualTo(new ShowComment(
                COMMENT_ID,
                1L,
                "Great show",
                5,
                CREATED_AT
        ));
    }
}
