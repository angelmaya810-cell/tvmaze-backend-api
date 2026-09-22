package com.coppel.tvmaze.comment.infrastructure.mongodb;

import com.coppel.tvmaze.comment.application.port.out.CommentStore;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CommentMongoConfigurationTest {

    @Autowired
    private CommentMongoRepository repository;

    @Autowired
    private CommentStore store;

    @Autowired
    private MongoConverter converter;

    @Test
    void configuresTheRepositoryAndMapsTheCommentDocument() {
        CommentDocument source = new CommentDocument(
                "68d17b9510b2ac45f2931234",
                1L,
                "Great show",
                5,
                Instant.parse("2026-09-22T12:00:00Z")
        );
        Document bson = new Document();

        converter.write(source, bson);
        CommentDocument restored = converter.read(CommentDocument.class, bson);

        assertThat(repository).isNotNull();
        assertThat(store).isInstanceOf(MongoCommentStore.class);
        assertThat(bson.getObjectId("_id").toHexString()).isEqualTo(source.id());
        assertThat(bson.get("show_id")).isEqualTo(1L);
        assertThat(bson.get("created_at")).isNotNull();
        assertThat(restored).isEqualTo(source);
    }
}
