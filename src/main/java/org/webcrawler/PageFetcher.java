package org.webcrawler;

public interface PageFetcher {

    FetchedPage fetch(String url);
}
