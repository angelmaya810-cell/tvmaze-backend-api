package com.coppel.tvmaze.show.application;

import com.coppel.tvmaze.show.application.port.out.ShowCatalogClient;
import com.coppel.tvmaze.show.domain.ShowSummary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowSearchService {

    private final ShowCatalogClient showCatalogClient;

    public ShowSearchService(ShowCatalogClient showCatalogClient) {
        this.showCatalogClient = showCatalogClient;
    }

    public List<ShowSummary> search(String searchQuery) {
        if (searchQuery == null || searchQuery.isBlank()) {
            throw new InvalidSearchQueryException();
        }

        return showCatalogClient.search(searchQuery.strip());
    }
}
