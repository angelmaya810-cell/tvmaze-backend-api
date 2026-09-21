package com.coppel.tvmaze.show.infrastructure.tvmaze;

import com.coppel.tvmaze.show.application.ShowCatalogUnavailableException;
import com.coppel.tvmaze.show.application.port.out.ShowCatalogClient;
import com.coppel.tvmaze.show.domain.ShowSummary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Component
public class TvMazeHttpClient implements ShowCatalogClient {

    private static final ParameterizedTypeReference<List<TvMazeSearchItem>> SEARCH_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public TvMazeHttpClient(@Qualifier("tvMazeRestClient") RestClient tvMazeRestClient) {
        this.restClient = tvMazeRestClient;
    }

    @Override
    public List<ShowSummary> search(String query) {
        try {
            List<TvMazeSearchItem> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/shows")
                            .queryParam("q", query)
                            .build())
                    .retrieve()
                    .body(SEARCH_RESPONSE_TYPE);

            if (response == null) {
                return List.of();
            }

            return response.stream()
                    .map(this::toShowSummary)
                    .toList();
        } catch (RestClientResponseException exception) {
            throw new ShowCatalogUnavailableException(
                    "TVMaze responded with HTTP " + exception.getStatusCode().value(),
                    exception
            );
        } catch (ResourceAccessException exception) {
            throw new ShowCatalogUnavailableException(
                    "TVMaze could not be reached",
                    exception
            );
        } catch (RestClientException exception) {
            throw new ShowCatalogUnavailableException(
                    "TVMaze returned an unreadable response",
                    exception
            );
        }
    }

    private ShowSummary toShowSummary(TvMazeSearchItem item) {
        if (item == null || item.show() == null || item.show().id() == null) {
            throw new ShowCatalogUnavailableException("TVMaze returned an invalid search response");
        }

        TvMazeShow show = item.show();
        return new ShowSummary(
                show.id(),
                show.name(),
                resolveChannel(show),
                show.summary(),
                show.genres()
        );
    }

    private String resolveChannel(TvMazeShow show) {
        if (show.network() != null && hasText(show.network().name())) {
            return show.network().name();
        }
        if (show.webChannel() != null && hasText(show.webChannel().name())) {
            return show.webChannel().name();
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
