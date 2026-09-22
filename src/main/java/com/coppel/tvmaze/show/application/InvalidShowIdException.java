package com.coppel.tvmaze.show.application;

public class InvalidShowIdException extends RuntimeException {

    public InvalidShowIdException() {
        super("showId must be greater than zero");
    }
}
