package org.webcrawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;

/**
 * Wrapper Class to fetch HTML-Structure of the page reachable under the selected URL.
 * Utilizes JSOUP-Library.
 */

public class HTMLFetcher {

    private static final int TIMEOUT_IN_MS = 10000;

    /**
     * Fetches HTML of the webpage located at the selected url.
     * Redirects are allowed.
     * Fails if connection is not established within 10 seconds.
     * @param url - page to fetch
     * @return Document object, containing HTML structure of the fetched page
     */
    public Document fetch(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .timeout(TIMEOUT_IN_MS)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .execute();
            if (isBroken(response.statusCode())) {
                System.err.println("[broken] " + url + " → HTTP " + response.statusCode());
                return null;
            }
            return response.parse();
        } catch (IOException e) {
            System.err.println("[broken] " + url + " → " + e.getMessage());
            return null;
        }
    }

    /**
     * Any status code above 400 is considered as broken.
     */
    public boolean isBroken(int statusCode) {
        return statusCode >= 400;
    }
}
