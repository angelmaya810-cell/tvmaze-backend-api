package com.coppel.tvmaze.show.application;

import com.coppel.tvmaze.comment.application.port.out.CommentStore;
import com.coppel.tvmaze.comment.domain.ShowComment;
import com.coppel.tvmaze.show.application.port.out.ShowCatalogClient;
import com.coppel.tvmaze.show.domain.ShowSearchResult;
import com.coppel.tvmaze.show.domain.ShowSummary;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShowSearchService {

    private final ShowCatalogClient showCatalogClient;
    private final CommentStore commentStore;

    public ShowSearchService(
            ShowCatalogClient showCatalogClient,
            CommentStore commentStore
    ) {
        this.showCatalogClient = showCatalogClient;
        this.commentStore = commentStore;
    }

    public List<ShowSearchResult> search(String searchQuery) {
        if (searchQuery == null || searchQuery.isBlank()) {
            throw new InvalidSearchQueryException();
        }

        List<ShowSummary> shows = showCatalogClient.search(searchQuery.strip());
        if (shows.isEmpty()) {
            return List.of();
        }

        List<Long> showIds = shows.stream()
                .map(ShowSummary::id)
                .distinct()
                .toList();
        Map<Long, List<ShowComment>> commentsByShowId = commentStore
                .findByShowIds(showIds)
                .stream()
                .collect(Collectors.groupingBy(
                        ShowComment::showId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return shows.stream()
                .map(show -> new ShowSearchResult(
                        show,
                        commentsByShowId.getOrDefault(show.id(), List.of())
                ))
                .toList();
    }
}
