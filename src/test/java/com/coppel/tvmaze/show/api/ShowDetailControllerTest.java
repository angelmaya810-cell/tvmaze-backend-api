package com.coppel.tvmaze.show.api;

import com.coppel.tvmaze.common.error.ApiExceptionHandler;
import com.coppel.tvmaze.show.application.ShowDetailService;
import com.coppel.tvmaze.show.application.port.out.ShowDetailsClient;
import com.coppel.tvmaze.show.domain.ShowDetails;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ShowDetailControllerTest {

    @Test
    void returnsTheCompleteShowObject() throws Exception {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("id", 1L);
        attributes.put("name", "Under the Dome");
        attributes.put("genres", List.of("Drama", "Science-Fiction"));
        attributes.put("network", Map.of("id", 2, "name", "CBS"));
        attributes.put("summary", null);
        ShowDetails show = new ShowDetails(1L, attributes);
        MockMvc mockMvc = mockMvc(showId -> Optional.of(show));

        mockMvc.perform(get("/api/v1/shows/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Under the Dome"))
                .andExpect(jsonPath("$.genres[1]").value("Science-Fiction"))
                .andExpect(jsonPath("$.network.name").value("CBS"))
                .andExpect(jsonPath("$.summary").isEmpty());
    }

    @Test
    void returnsNotFoundForUnknownShows() throws Exception {
        MockMvc mockMvc = mockMvc(showId -> Optional.empty());

        mockMvc.perform(get("/api/v1/shows/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Show not found"))
                .andExpect(jsonPath("$.detail").value("Show 999999 was not found"));
    }

    @Test
    void rejectsNonPositiveShowIds() throws Exception {
        MockMvc mockMvc = mockMvc(showId -> Optional.empty());

        mockMvc.perform(get("/api/v1/shows/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Request validation failed"));
    }

    private MockMvc mockMvc(ShowDetailsClient client) {
        ShowDetailService service = new ShowDetailService(client);
        ShowDetailController controller = new ShowDetailController(service);
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }
}
