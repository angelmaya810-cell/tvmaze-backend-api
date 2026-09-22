package com.coppel.tvmaze.show.infrastructure.tvmaze;

import com.coppel.tvmaze.show.application.ShowCatalogUnavailableException;
import com.coppel.tvmaze.show.domain.ShowSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TvMazeHttpClientTest {

    private MockRestServiceServer server;
    private TvMazeHttpClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new TvMazeHttpClient(builder.baseUrl("https://api.tvmaze.test").build());
    }

    @Test
    void mapsNetworksAndWebChannelsWithoutChangingResultOrder() {
        server.expect(once(), requestTo("https://api.tvmaze.test/search/shows?q=girls"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [
                          {
                            "score": 0.9,
                            "show": {
                              "id": 139,
                              "name": "Girls",
                              "network": {"name": "HBO"},
                              "webChannel": null,
                              "summary": "<p>Summary</p>",
                              "genres": ["Drama", "Romance"]
                            }
                          },
                          {
                            "score": 0.8,
                            "show": {
                              "id": 49334,
                              "name": "Shining Girls",
                              "network": null,
                              "webChannel": {"name": "Apple TV"},
                              "summary": null,
                              "genres": ["Crime"]
                            }
                          },
                          {
                            "score": 0.7,
                            "show": {
                              "id": 999,
                              "name": "Independent Show",
                              "network": null,
                              "webChannel": null,
                              "summary": null,
                              "genres": null
                            }
                          }
                        ]
                        """, MediaType.APPLICATION_JSON));

        List<ShowSummary> result = client.search("girls");

        assertThat(result).containsExactly(
                new ShowSummary(139L, "Girls", "HBO", "<p>Summary</p>",
                        List.of("Drama", "Romance")),
                new ShowSummary(49334L, "Shining Girls", "Apple TV", null,
                        List.of("Crime")),
                new ShowSummary(999L, "Independent Show", null, null, List.of())
        );
        server.verify();
    }

    @Test
    void encodesTheSearchQuery() {
        server.expect(once(), requestTo("https://api.tvmaze.test/search/shows?q=game%20of%20thrones"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThat(client.search("game of thrones")).isEmpty();
        server.verify();
    }

    @Test
    void translatesUpstreamErrors() {
        server.expect(once(), requestTo("https://api.tvmaze.test/search/shows?q=girls"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.search("girls"))
                .isInstanceOf(ShowCatalogUnavailableException.class)
                .hasMessage("TVMaze responded with HTTP 500");
        server.verify();
    }

    @Test
    void translatesUnreadableResponses() {
        server.expect(once(), requestTo("https://api.tvmaze.test/search/shows?q=girls"))
                .andRespond(withSuccess("{not-json}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.search("girls"))
                .isInstanceOf(ShowCatalogUnavailableException.class)
                .hasMessage("TVMaze returned an unreadable response");
        server.verify();
    }

    @Test
    void returnsTheCompleteShowObject() {
        server.expect(once(), requestTo("https://api.tvmaze.test/shows/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "id": 1,
                          "url": "https://www.tvmaze.com/shows/1/under-the-dome",
                          "name": "Under the Dome",
                          "genres": ["Drama", "Science-Fiction", "Thriller"],
                          "network": {
                            "id": 2,
                            "name": "CBS",
                            "country": {"code": "US"}
                          },
                          "summary": null,
                          "_links": {
                            "self": {"href": "https://api.tvmaze.com/shows/1"}
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        var result = client.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().id()).isEqualTo(1L);
        assertThat(result.orElseThrow().attributes())
                .containsEntry("name", "Under the Dome")
                .containsEntry("summary", null)
                .containsKey("_links");
        Object networkAttribute = result.orElseThrow().attributes().get("network");
        assertThat(networkAttribute).isInstanceOf(Map.class);
        Map<?, ?> network = (Map<?, ?>) networkAttribute;
        assertThat(network.get("name")).isEqualTo("CBS");
        server.verify();
    }

    @Test
    void returnsEmptyForUnknownShows() {
        server.expect(once(), requestTo("https://api.tvmaze.test/shows/999999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.findById(999999L)).isEmpty();
        server.verify();
    }

    @Test
    void rejectsResponsesForADifferentShow() {
        server.expect(once(), requestTo("https://api.tvmaze.test/shows/1"))
                .andRespond(withSuccess("{\"id\": 2}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.findById(1L))
                .isInstanceOf(ShowCatalogUnavailableException.class)
                .hasMessage("TVMaze returned an invalid show response");
        server.verify();
    }
}
