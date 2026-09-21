package com.coppel.tvmaze.show.application;

public class ShowCatalogUnavailableException extends RuntimeException {

    public ShowCatalogUnavailableException(String message) {
        super(message);
    }

    public ShowCatalogUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
