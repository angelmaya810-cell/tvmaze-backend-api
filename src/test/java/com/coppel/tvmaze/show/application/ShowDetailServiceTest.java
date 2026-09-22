package com.coppel.tvmaze.show.application;

import com.coppel.tvmaze.show.application.port.out.ShowDetailsCache;
import com.coppel.tvmaze.show.application.port.out.ShowDetailsClient;
import com.coppel.tvmaze.show.domain.ShowDetails;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
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
        when(cache.findById(1L)).thenReturn(Optional.of(cached));
        ShowDetailService service = new ShowDetailService(client, cache);

        assertThat(service.findById(1L)).isSameAs(cached);

        verify(cache).findById(1L);
        verify(cache, never()).save(cached);
        verifyNoInteractions(client);
    }

    @Test
    void obtainsAndCachesAShowWhenItIsNotCached() {
        ShowDetails fetched = showDetails(1L);
        ShowDetailsClient client = mock(ShowDetailsClient.class);
        ShowDetailsCache cache = mock(ShowDetailsCache.class);
        when(cache.findById(1L)).thenReturn(Optional.empty());
        when(client.findById(1L)).thenReturn(Optional.of(fetched));
        ShowDetailService service = new ShowDetailService(client, cache);

        assertThat(service.findById(1L)).isSameAs(fetched);

        verify(cache).findById(1L);
        verify(client).findById(1L);
        verify(cache).save(fetched);
    }

    @Test
    void rejectsNonPositiveIdsBeforeCallingTheCatalog() {
        ShowDetailsClient client = mock(ShowDetailsClient.class);
        ShowDetailsCache cache = mock(ShowDetailsCache.class);
        ShowDetailService service = new ShowDetailService(client, cache);

        assertThatThrownBy(() -> service.findById(0L))
                .isInstanceOf(InvalidShowIdException.class)
                .hasMessage("showId must be greater than zero");

        verifyNoInteractions(cache, client);
    }

    @Test
    void reportsMissingShows() {
        ShowDetailsClient client = mock(ShowDetailsClient.class);
        ShowDetailsCache cache = mock(ShowDetailsCache.class);
        when(cache.findById(999999L)).thenReturn(Optional.empty());
        when(client.findById(999999L)).thenReturn(Optional.empty());
        ShowDetailService service = new ShowDetailService(client, cache);

        assertThatThrownBy(() -> service.findById(999999L))
                .isInstanceOf(ShowNotFoundException.class)
                .hasMessage("Show 999999 was not found");

        verify(cache).findById(999999L);
        verify(client).findById(999999L);
        verify(cache, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private ShowDetails showDetails(long showId) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("id", showId);
        attributes.put("name", "Under the Dome");
        return new ShowDetails(showId, attributes);
    }
}
