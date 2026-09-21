package com.coppel.tvmaze.show.application;

import com.coppel.tvmaze.show.application.port.out.ShowCatalogClient;
import com.coppel.tvmaze.show.domain.ShowSummary;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShowSearchServiceTest {

    @Test
    void trimsTheQueryAndReturnsCatalogResults() {
        AtomicReference<String> receivedQuery = new AtomicReference<>();
        ShowSummary expected = new ShowSummary(
                139L,
                "Girls",
                "HBO",
                "Summary",
                List.of("Drama")
        );
        ShowCatalogClient client = query -> {
            receivedQuery.set(query);
            return List.of(expected);
        };
        ShowSearchService service = new ShowSearchService(client);

        List<ShowSummary> result = service.search("  girls  ");

        assertThat(receivedQuery).hasValue("girls");
        assertThat(result).containsExactly(expected);
    }

    @Test
    void rejectsBlankQueriesBeforeCallingTheCatalog() {
        ShowCatalogClient client = query -> {
            throw new AssertionError("The catalog must not be called");
        };
        ShowSearchService service = new ShowSearchService(client);

        assertThatThrownBy(() -> service.search("   "))
                .isInstanceOf(InvalidSearchQueryException.class)
                .hasMessage("search_query must not be blank");
    }
}
