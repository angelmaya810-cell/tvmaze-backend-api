package com.coppel.tvmaze.show.application;

import com.coppel.tvmaze.show.application.port.out.ShowDetailsClient;
import com.coppel.tvmaze.show.domain.ShowDetails;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShowDetailServiceTest {

    @Test
    void returnsAnExistingShow() {
        ShowDetails expected = showDetails(1L);
        ShowDetailsClient client = showId -> Optional.of(expected);
        ShowDetailService service = new ShowDetailService(client);

        assertThat(service.findById(1L)).isSameAs(expected);
    }

    @Test
    void rejectsNonPositiveIdsBeforeCallingTheCatalog() {
        ShowDetailsClient client = showId -> {
            throw new AssertionError("The catalog must not be called");
        };
        ShowDetailService service = new ShowDetailService(client);

        assertThatThrownBy(() -> service.findById(0L))
                .isInstanceOf(InvalidShowIdException.class)
                .hasMessage("showId must be greater than zero");
    }

    @Test
    void reportsMissingShows() {
        ShowDetailsClient client = showId -> Optional.empty();
        ShowDetailService service = new ShowDetailService(client);

        assertThatThrownBy(() -> service.findById(999999L))
                .isInstanceOf(ShowNotFoundException.class)
                .hasMessage("Show 999999 was not found");
    }

    private ShowDetails showDetails(long showId) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("id", showId);
        attributes.put("name", "Under the Dome");
        return new ShowDetails(showId, attributes);
    }
}
