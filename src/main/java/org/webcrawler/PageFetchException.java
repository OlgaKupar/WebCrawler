package org.webcrawler;

public class PageFetchException extends RuntimeException {

    public PageFetchException(String message) {
        super(message);
    }

    public PageFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
