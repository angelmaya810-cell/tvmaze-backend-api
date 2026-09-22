package com.coppel.tvmaze.show.infrastructure.mongodb;

import com.coppel.tvmaze.show.domain.ShowDetails;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MongoShowDetailsCacheTest {

    private static final Instant CACHED_AT = Instant.parse("2026-09-22T12:00:00Z");

    @Test
    void returnsTheCompleteCachedShow() {
        CachedShowMongoRepository repository = mock(CachedShowMongoRepository.class);
        CachedShowDocument document = new CachedShowDocument(1L, attributes(), CACHED_AT);
        when(repository.findById(1L)).thenReturn(Optional.of(document));
        MongoShowDetailsCache cache = new MongoShowDetailsCache(repository, fixedClock());

        Optional<ShowDetails> result = cache.findById(1L);

        assertThat(result).contains(new ShowDetails(1L, attributes()));
        verify(repository).findById(1L);
    }

    @Test
    void returnsEmptyWhenTheShowIsNotCached() {
        CachedShowMongoRepository repository = mock(CachedShowMongoRepository.class);
        when(repository.findById(404L)).thenReturn(Optional.empty());
        MongoShowDetailsCache cache = new MongoShowDetailsCache(repository, fixedClock());

        assertThat(cache.findById(404L)).isEmpty();
    }

    @Test
    void savesTheCompleteShowWithTheCurrentInstant() {
        CachedShowMongoRepository repository = mock(CachedShowMongoRepository.class);
        MongoShowDetailsCache cache = new MongoShowDetailsCache(repository, fixedClock());
        ShowDetails show = new ShowDetails(1L, attributes());

        cache.save(show);

        ArgumentCaptor<CachedShowDocument> captor =
                ArgumentCaptor.forClass(CachedShowDocument.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue()).isEqualTo(
                new CachedShowDocument(1L, attributes(), CACHED_AT)
        );
    }

    private Clock fixedClock() {
        return Clock.fixed(CACHED_AT, ZoneOffset.UTC);
    }

    private Map<String, Object> attributes() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("id", 1L);
        attributes.put("name", "Under the Dome");
        attributes.put("genres", List.of("Drama", "Science-Fiction"));
        attributes.put("network", Map.of("id", 2, "name", "CBS"));
        attributes.put("summary", null);
        return attributes;
    }
}
