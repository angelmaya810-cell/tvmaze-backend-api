package com.coppel.tvmaze.show.infrastructure.tvmaze;

import com.coppel.tvmaze.show.application.ShowCatalogUnavailableException;
import com.coppel.tvmaze.show.application.port.out.ShowCatalogClient;
import com.coppel.tvmaze.show.application.port.out.ShowDetailsClient;
import com.coppel.tvmaze.show.domain.ShowDetails;
import com.coppel.tvmaze.show.domain.ShowSummary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;
import java.util.List;
import java.util.Optional;

@Component
public class TvMazeHttpClient implements ShowCatalogClient, ShowDetailsClient {

    private static final ParameterizedTypeReference<List<TvMazeSearchItem>> SEARCH_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<Map<String, Object>> SHOW_RESPONSE_TYPE =
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

    @Override
    public Optional<ShowDetails> findById(long showId) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri("/shows/{showId}", showId)
                    .retrieve()
                    .body(SHOW_RESPONSE_TYPE);

            return Optional.of(toShowDetails(showId, response));
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
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

    private ShowDetails toShowDetails(long requestedId, Map<String, Object> response) {
        if (response == null || !(response.get("id") instanceof Number responseId)
                || responseId.longValue() != requestedId) {
            throw new ShowCatalogUnavailableException("TVMaze returned an invalid show response");
        }

        return new ShowDetails(requestedId, response);
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
