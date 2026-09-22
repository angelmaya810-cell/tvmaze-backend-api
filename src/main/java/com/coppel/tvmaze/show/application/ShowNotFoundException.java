package com.coppel.tvmaze.show.application;

public class ShowNotFoundException extends RuntimeException {

    public ShowNotFoundException(long showId) {
        super("Show " + showId + " was not found");
    }
}
