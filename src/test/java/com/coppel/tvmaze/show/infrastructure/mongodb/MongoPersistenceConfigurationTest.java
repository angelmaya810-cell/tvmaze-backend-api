package com.coppel.tvmaze.show.infrastructure.mongodb;

import com.coppel.tvmaze.show.application.port.out.ShowDetailsCache;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MongoPersistenceConfigurationTest {

    @Autowired
    private CachedShowMongoRepository repository;

    @Autowired
    private MongoConverter converter;

    @Autowired
    private ShowDetailsCache cache;

    @Test
    void configuresTheRepositoryAndMapsTheCompleteShowDocument() {
        Map<String, Object> show = new LinkedHashMap<>();
        show.put("id", 1L);
        show.put("name", "Under the Dome");
        show.put("genres", List.of("Drama", "Science-Fiction"));
        show.put("network", Map.of("id", 2, "name", "CBS"));
        show.put("summary", null);
        Instant cachedAt = Instant.parse("2026-09-21T12:00:00Z");
        CachedShowDocument source = new CachedShowDocument(1L, show, cachedAt);
        Document bson = new Document();

        converter.write(source, bson);
        CachedShowDocument restored = converter.read(CachedShowDocument.class, bson);

        assertThat(repository).isNotNull();
        assertThat(cache).isInstanceOf(MongoShowDetailsCache.class);
        assertThat(bson.get("_id")).isEqualTo(1L);
        assertThat(bson.get("cached_at")).isNotNull();
        assertThat(restored).isEqualTo(source);
        assertThat(restored.show().get("network")).isInstanceOf(Map.class);
        Map<?, ?> restoredNetwork = (Map<?, ?>) restored.show().get("network");
        assertThat(restoredNetwork.get("name")).isEqualTo("CBS");
    }
}
