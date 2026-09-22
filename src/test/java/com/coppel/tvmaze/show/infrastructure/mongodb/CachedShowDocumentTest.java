package com.coppel.tvmaze.show.infrastructure.mongodb;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CachedShowDocumentTest {

    @Test
    void createsAnImmutableSnapshotWithoutDroppingNullValues() {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("id", 1L);
        source.put("name", "Under the Dome");
        source.put("summary", null);
        Instant cachedAt = Instant.parse("2026-09-21T12:00:00Z");

        CachedShowDocument document = new CachedShowDocument(1L, source, cachedAt);
        source.put("name", "Changed outside the document");

        assertThat(document.id()).isEqualTo(1L);
        assertThat(document.show())
                .containsEntry("name", "Under the Dome")
                .containsEntry("summary", null);
        assertThat(document.cachedAt()).isEqualTo(cachedAt);
        assertThatThrownBy(() -> document.show().put("status", "Ended"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsInvalidCacheDocuments() {
        Instant cachedAt = Instant.parse("2026-09-21T12:00:00Z");

        assertThatThrownBy(() -> new CachedShowDocument(0L, Map.of(), cachedAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("id must be greater than zero");
        assertThatThrownBy(() -> new CachedShowDocument(1L, null, cachedAt))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("show must not be null");
        assertThatThrownBy(() -> new CachedShowDocument(1L, Map.of(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("cachedAt must not be null");
    }
}
