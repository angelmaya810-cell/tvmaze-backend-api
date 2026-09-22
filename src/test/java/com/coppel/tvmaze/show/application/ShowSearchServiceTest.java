package com.coppel.tvmaze.show.application;

import com.coppel.tvmaze.comment.application.port.out.CommentStore;
import com.coppel.tvmaze.comment.domain.ShowComment;
import com.coppel.tvmaze.show.application.port.out.ShowCatalogClient;
import com.coppel.tvmaze.show.domain.ShowSearchResult;
import com.coppel.tvmaze.show.domain.ShowSummary;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ShowSearchServiceTest {

    @Test
    void enrichesCatalogResultsWithOneBatchCommentLookup() {
        ShowSummary girls = show(139L, "Girls");
        ShowSummary other = show(42L, "Other");
        ShowComment first = comment("1", 139L, "First", 4, "2026-09-22T12:00:00Z");
        ShowComment second = comment("2", 139L, "Second", 5, "2026-09-22T13:00:00Z");
        ShowCatalogClient client = mock(ShowCatalogClient.class);
        CommentStore store = mock(CommentStore.class);
        when(client.search("girls")).thenReturn(List.of(girls, other));
        when(store.findByShowIds(List.of(139L, 42L)))
                .thenReturn(List.of(first, second));
        ShowSearchService service = new ShowSearchService(client, store);

        List<ShowSearchResult> result = service.search("  girls  ");

        assertThat(result).extracting(item -> item.show().id())
                .containsExactly(139L, 42L);
        assertThat(result.getFirst().comments()).containsExactly(first, second);
        assertThat(result.get(1).comments()).isEmpty();
        verify(client).search("girls");
        verify(store).findByShowIds(List.of(139L, 42L));
    }

    @Test
    void skipsMongoWhenTheCatalogReturnsNoShows() {
        ShowCatalogClient client = mock(ShowCatalogClient.class);
        CommentStore store = mock(CommentStore.class);
        when(client.search("missing")).thenReturn(List.of());
        ShowSearchService service = new ShowSearchService(client, store);

        assertThat(service.search("missing")).isEmpty();

        verify(client).search("missing");
        verifyNoInteractions(store);
    }

    @Test
    void rejectsBlankQueriesBeforeCallingDependencies() {
        ShowCatalogClient client = mock(ShowCatalogClient.class);
        CommentStore store = mock(CommentStore.class);
        ShowSearchService service = new ShowSearchService(client, store);

        assertThatThrownBy(() -> service.search("   "))
                .isInstanceOf(InvalidSearchQueryException.class)
                .hasMessage("search_query must not be blank");

        verifyNoInteractions(client, store);
    }

    private ShowSummary show(long id, String name) {
        return new ShowSummary(id, name, "HBO", "Summary", List.of("Drama"));
    }

    private ShowComment comment(
            String id,
            long showId,
            String text,
            int rating,
            String createdAt
    ) {
        return new ShowComment(id, showId, text, rating, Instant.parse(createdAt));
    }
}
