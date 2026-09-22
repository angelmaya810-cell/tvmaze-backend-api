package com.coppel.tvmaze.show.application;

import com.coppel.tvmaze.comment.application.port.out.CommentStore;
import com.coppel.tvmaze.comment.domain.ShowComment;
import com.coppel.tvmaze.show.application.port.out.ShowDetailsCache;
import com.coppel.tvmaze.show.application.port.out.ShowDetailsClient;
import com.coppel.tvmaze.show.domain.ShowDetailResult;
import com.coppel.tvmaze.show.domain.ShowDetails;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ShowDetailServiceTest {

    @Test
    void returnsACachedShowWithoutCallingTheCatalog() {
        ShowDetails cached = showDetails(1L);
        ShowDetailsClient client = mock(ShowDetailsClient.class);
        ShowDetailsCache cache = mock(ShowDetailsCache.class);
        CommentStore commentStore = mock(CommentStore.class);
        when(cache.findById(1L)).thenReturn(Optional.of(cached));
        ShowDetailService service = new ShowDetailService(client, cache, commentStore);

        assertThat(service.findById(1L)).isSameAs(cached);

        verify(cache).findById(1L);
        verify(cache, never()).save(cached);
        verifyNoInteractions(client);
        verifyNoInteractions(commentStore);
    }

    @Test
    void returnsCurrentCommentsWithTheCachedShowDetail() {
        ShowDetails cached = showDetails(1L);
        ShowComment comment = new ShowComment(
                "68d17b9510b2ac45f2931234",
                1L,
                "Great show",
                5,
                Instant.parse("2026-09-22T12:00:00Z")
        );
        ShowDetailsClient client = mock(ShowDetailsClient.class);
        ShowDetailsCache cache = mock(ShowDetailsCache.class);
        CommentStore commentStore = mock(CommentStore.class);
        when(cache.findById(1L)).thenReturn(Optional.of(cached));
        when(commentStore.findByShowIds(List.of(1L))).thenReturn(List.of(comment));
        ShowDetailService service = new ShowDetailService(client, cache, commentStore);

        ShowDetailResult result = service.findWithCommentsById(1L);

        assertThat(result.show()).isSameAs(cached);
        assertThat(result.comments()).containsExactly(comment);
        verify(commentStore).findByShowIds(List.of(1L));
        verifyNoInteractions(client);
    }

    @Test
    void obtainsAndCachesAShowWhenItIsNotCached() {
        ShowDetails fetched = showDetails(1L);
        ShowDetailsClient client = mock(ShowDetailsClient.class);
        ShowDetailsCache cache = mock(ShowDetailsCache.class);
        CommentStore commentStore = mock(CommentStore.class);
        when(cache.findById(1L)).thenReturn(Optional.empty());
        when(client.findById(1L)).thenReturn(Optional.of(fetched));
        ShowDetailService service = new ShowDetailService(client, cache, commentStore);

        assertThat(service.findById(1L)).isSameAs(fetched);

        verify(cache).findById(1L);
        verify(client).findById(1L);
        verify(cache).save(fetched);
        verifyNoInteractions(commentStore);
    }

    @Test
    void rejectsNonPositiveIdsBeforeCallingTheCatalog() {
        ShowDetailsClient client = mock(ShowDetailsClient.class);
        ShowDetailsCache cache = mock(ShowDetailsCache.class);
        CommentStore commentStore = mock(CommentStore.class);
        ShowDetailService service = new ShowDetailService(client, cache, commentStore);

        assertThatThrownBy(() -> service.findById(0L))
                .isInstanceOf(InvalidShowIdException.class)
                .hasMessage("showId must be greater than zero");

        verifyNoInteractions(cache, client, commentStore);
    }

    @Test
    void reportsMissingShows() {
        ShowDetailsClient client = mock(ShowDetailsClient.class);
        ShowDetailsCache cache = mock(ShowDetailsCache.class);
        CommentStore commentStore = mock(CommentStore.class);
        when(cache.findById(999999L)).thenReturn(Optional.empty());
        when(client.findById(999999L)).thenReturn(Optional.empty());
        ShowDetailService service = new ShowDetailService(client, cache, commentStore);

        assertThatThrownBy(() -> service.findById(999999L))
                .isInstanceOf(ShowNotFoundException.class)
                .hasMessage("Show 999999 was not found");

        verify(cache).findById(999999L);
        verify(client).findById(999999L);
        verify(cache, never()).save(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(commentStore);
    }

    private ShowDetails showDetails(long showId) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("id", showId);
        attributes.put("name", "Under the Dome");
        return new ShowDetails(showId, attributes);
    }
}
