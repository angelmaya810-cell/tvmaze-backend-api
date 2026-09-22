package com.coppel.tvmaze.show.api;

import com.coppel.tvmaze.common.error.ApiExceptionHandler;
import com.coppel.tvmaze.show.application.ShowSearchService;
import com.coppel.tvmaze.show.application.port.out.ShowCatalogClient;
import com.coppel.tvmaze.show.domain.ShowSummary;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ShowSearchControllerTest {

    @Test
    void returnsThePublicSearchContract() throws Exception {
        ShowCatalogClient client = query -> List.of(new ShowSummary(
                139L,
                "Girls",
                "HBO",
                "<p>Summary</p>",
                List.of("Drama", "Romance")
        ));
        MockMvc mockMvc = mockMvc(client);

        mockMvc.perform(get("/api/v1/shows/search")
                        .queryParam("search_query", "girls"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[0].id").value(139))
                .andExpect(jsonPath("$[0].name").value("Girls"))
                .andExpect(jsonPath("$[0].channel").value("HBO"))
                .andExpect(jsonPath("$[0].summary").value("<p>Summary</p>"))
                .andExpect(jsonPath("$[0].genres[0]").value("Drama"))
                .andExpect(jsonPath("$[0].genres[1]").value("Romance"));
    }

    @Test
    void rejectsBlankSearchQueries() throws Exception {
        MockMvc mockMvc = mockMvc(query -> List.of());

        mockMvc.perform(get("/api/v1/shows/search")
                .queryParam("search_query", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Request validation failed"))
                .andExpect(jsonPath("$.detail")
                        .value("search_query must not be blank"));
    }

    @Test
    void rejectsRequestsWithoutTheRequiredParameter() throws Exception {
        MockMvc mockMvc = mockMvc(query -> List.of());

        mockMvc.perform(get("/api/v1/shows/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Missing request parameter"))
                .andExpect(jsonPath("$.detail").value("search_query is required"));
    }

    private MockMvc mockMvc(ShowCatalogClient client) {
        ShowSearchService service = new ShowSearchService(client);
        ShowSearchController controller = new ShowSearchController(service);
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }
}
