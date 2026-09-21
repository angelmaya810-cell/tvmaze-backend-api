package com.coppel.tvmaze.show.application;

public class InvalidSearchQueryException extends RuntimeException {

    public InvalidSearchQueryException() {
        super("search_query must not be blank");
    }
}
