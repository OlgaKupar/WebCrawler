package org.webcrawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

public class JsoupPageFetcher implements PageFetcher {

    private static final int TIMEOUT_IN_MS = 10000;

    @Override
    public FetchedPage fetch(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .timeout(TIMEOUT_IN_MS)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .execute();
            if (isBroken(response.statusCode())) {
                throw new PageFetchException("HTTP " + response.statusCode());
            }
            return new FetchedPage(response.body(), url);
        } catch (IOException e) {
            throw new PageFetchException("Connection error: " + e.getMessage(), e);
        }
    }

    boolean isBroken(int statusCode) {
        return statusCode >= 400;
    }
}
