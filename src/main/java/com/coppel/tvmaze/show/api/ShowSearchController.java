package com.coppel.tvmaze.show.api;

import com.coppel.tvmaze.show.application.ShowSearchService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shows")
public class ShowSearchController {

    private final ShowSearchService showSearchService;

    public ShowSearchController(ShowSearchService showSearchService) {
        this.showSearchService = showSearchService;
    }

    @GetMapping("/search")
    public List<ShowSearchResponse> search(
            @RequestParam("search_query")
            @NotBlank(message = "search_query must not be blank") String searchQuery
    ) {
        return showSearchService.search(searchQuery).stream()
                .map(ShowSearchResponse::from)
                .toList();
    }
}
