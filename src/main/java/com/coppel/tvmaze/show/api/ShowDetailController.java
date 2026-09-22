package com.coppel.tvmaze.show.api;

import com.coppel.tvmaze.show.application.ShowDetailService;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shows")
public class ShowDetailController {

    private final ShowDetailService showDetailService;

    public ShowDetailController(ShowDetailService showDetailService) {
        this.showDetailService = showDetailService;
    }

    @GetMapping("/{showId}")
    public ShowDetailResponse findById(
            @PathVariable @Positive(message = "showId must be greater than zero") long showId
    ) {
        return ShowDetailResponse.from(showDetailService.findWithCommentsById(showId));
    }
}
