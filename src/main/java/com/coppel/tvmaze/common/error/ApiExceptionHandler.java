package com.coppel.tvmaze.common.error;

import com.coppel.tvmaze.show.application.InvalidSearchQueryException;
import com.coppel.tvmaze.show.application.InvalidShowIdException;
import com.coppel.tvmaze.show.application.ShowCatalogUnavailableException;
import com.coppel.tvmaze.show.application.ShowNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidSearchQueryException.class)
    public ResponseEntity<ProblemDetail> handleInvalidSearchQuery(
            InvalidSearchQueryException exception
    ) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid search query", exception.getMessage());
    }

    @ExceptionHandler(InvalidShowIdException.class)
    public ResponseEntity<ProblemDetail> handleInvalidShowId(
            InvalidShowIdException exception
    ) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid show ID", exception.getMessage());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleMethodValidation(
            HandlerMethodValidationException exception
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                "One or more request parameters are invalid"
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingRequestParameter(
            MissingServletRequestParameterException exception
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Missing request parameter",
                exception.getParameterName() + " is required"
        );
    }

    @ExceptionHandler(ShowCatalogUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleShowCatalogUnavailable(
            ShowCatalogUnavailableException exception
    ) {
        return problem(HttpStatus.BAD_GATEWAY, "Show catalog unavailable", exception.getMessage());
    }

    @ExceptionHandler(ShowNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleShowNotFound(
            ShowNotFoundException exception
    ) {
        return problem(HttpStatus.NOT_FOUND, "Show not found", exception.getMessage());
    }

    private ResponseEntity<ProblemDetail> problem(
            HttpStatus status,
            String title,
            String detail
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        return ResponseEntity.status(status).body(problemDetail);
    }
}
