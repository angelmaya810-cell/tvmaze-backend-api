package com.coppel.tvmaze.comment.application;

import com.coppel.tvmaze.comment.application.port.out.CommentStore;
import com.coppel.tvmaze.comment.domain.ShowComment;
import com.coppel.tvmaze.show.application.ShowDetailService;
import com.coppel.tvmaze.show.application.ShowNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CommentServiceTest {

    @Test
    void validatesTheShowAndPersistsANormalizedComment() {
        ShowDetailService showDetailService = mock(ShowDetailService.class);
        CommentStore store = mock(CommentStore.class);
        ShowComment saved = new ShowComment(
                "68d17b9510b2ac45f2931234",
                1L,
                "Great show",
                5,
                Instant.parse("2026-09-22T12:00:00Z")
        );
        when(store.save(1L, "Great show", 5)).thenReturn(saved);
        CommentService service = new CommentService(showDetailService, store);

        ShowComment result = service.create(1L, "  Great show  ", 5);

        assertThat(result).isSameAs(saved);
        verify(showDetailService).findById(1L);
        verify(store).save(1L, "Great show", 5);
    }

    @Test
    void rejectsInvalidInputBeforeCallingDependencies() {
        ShowDetailService showDetailService = mock(ShowDetailService.class);
        CommentStore store = mock(CommentStore.class);
        CommentService service = new CommentService(showDetailService, store);

        assertThatThrownBy(() -> service.create(1L, "   ", 5))
                .isInstanceOf(InvalidCommentException.class)
                .hasMessage("comment must not be blank");
        assertThatThrownBy(() -> service.create(1L, "x".repeat(1_001), 5))
                .isInstanceOf(InvalidCommentException.class)
                .hasMessage("comment must not exceed 1000 characters");
        assertThatThrownBy(() -> service.create(1L, "Good", 6))
                .isInstanceOf(InvalidCommentException.class)
                .hasMessage("rating must be between 0 and 5");
        assertThatThrownBy(() -> service.create(1L, "Good", -1))
                .isInstanceOf(InvalidCommentException.class)
                .hasMessage("rating must be between 0 and 5");
        assertThatThrownBy(() -> service.create(1L, "Good", null))
                .isInstanceOf(InvalidCommentException.class)
                .hasMessage("rating must be between 0 and 5");

        verifyNoInteractions(showDetailService, store);
    }

    @Test
    void doesNotPersistACommentForAnUnknownShow() {
        ShowDetailService showDetailService = mock(ShowDetailService.class);
        CommentStore store = mock(CommentStore.class);
        when(showDetailService.findById(999999L))
                .thenThrow(new ShowNotFoundException(999999L));
        CommentService service = new CommentService(showDetailService, store);

        assertThatThrownBy(() -> service.create(999999L, "Unknown", 3))
                .isInstanceOf(ShowNotFoundException.class)
                .hasMessage("Show 999999 was not found");

        verifyNoInteractions(store);
    }
}
