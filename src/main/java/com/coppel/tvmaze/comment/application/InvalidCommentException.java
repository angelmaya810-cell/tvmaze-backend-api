package com.coppel.tvmaze.comment.application;

public class InvalidCommentException extends RuntimeException {

    public InvalidCommentException(String message) {
        super(message);
    }
}
