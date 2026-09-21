package com.coppel.tvmaze.show.application.port.out;

import com.coppel.tvmaze.show.domain.ShowSummary;

import java.util.List;

public interface ShowCatalogClient {

    List<ShowSummary> search(String query);
}
