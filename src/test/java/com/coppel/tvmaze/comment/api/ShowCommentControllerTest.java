package com.coppel.tvmaze.comment.api;

import com.coppel.tvmaze.comment.application.CommentService;
import com.coppel.tvmaze.comment.domain.ShowComment;
import com.coppel.tvmaze.common.error.ApiExceptionHandler;
import com.coppel.tvmaze.show.application.ShowNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ShowCommentControllerTest {

    @Test
    void createsAComment() throws Exception {
        CommentService service = mock(CommentService.class);
        when(service.create(1L, "Great show", 5)).thenReturn(new ShowComment(
                "68d17b9510b2ac45f2931234",
                1L,
                "Great show",
                5,
                Instant.parse("2026-09-22T12:00:00Z")
        ));
        MockMvc mockMvc = mockMvc(service);

        mockMvc.perform(post("/api/v1/shows/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "comment": "Great show",
                                  "rating": 5
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.commentId").value("68d17b9510b2ac45f2931234"))
                .andExpect(jsonPath("$.showId").value(1));
    }

    @Test
    void rejectsAnEmptyComment() throws Exception {
        CommentService service = mock(CommentService.class);
        MockMvc mockMvc = mockMvc(service);

        mockMvc.perform(post("/api/v1/shows/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment": "   ", "rating": 5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Request validation failed"))
                .andExpect(jsonPath("$.detail").value("comment must not be blank"));

        verifyNoInteractions(service);
    }

    @Test
    void rejectsARatingOutsideTheAllowedRange() throws Exception {
        CommentService service = mock(CommentService.class);
        MockMvc mockMvc = mockMvc(service);

        mockMvc.perform(post("/api/v1/shows/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment": "Great show", "rating": 6}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("rating must be between 0 and 5"));

        verifyNoInteractions(service);
    }

    @Test
    void requiresARating() throws Exception {
        CommentService service = mock(CommentService.class);
        MockMvc mockMvc = mockMvc(service);

        mockMvc.perform(post("/api/v1/shows/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment": "Great show"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("rating is required"));

        verifyNoInteractions(service);
    }

    @Test
    void rejectsANonPositiveShowId() throws Exception {
        CommentService service = mock(CommentService.class);
        MockMvc mockMvc = mockMvc(service);

        mockMvc.perform(post("/api/v1/shows/0/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment": "Great show", "rating": 5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("showId must be greater than zero"));

        verifyNoInteractions(service);
    }

    @Test
    void reportsAnUnknownShow() throws Exception {
        CommentService service = mock(CommentService.class);
        when(service.create(999999L, "Unknown", 3))
                .thenThrow(new ShowNotFoundException(999999L));
        MockMvc mockMvc = mockMvc(service);

        mockMvc.perform(post("/api/v1/shows/999999/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment": "Unknown", "rating": 3}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Show not found"));
    }

    private MockMvc mockMvc(CommentService service) {
        return MockMvcBuilders.standaloneSetup(new ShowCommentController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }
}
