package org.webcrawler;

import java.util.List;
import java.util.Optional;

public class ParsedPage {

    private final String url;
    private final int depth;
    private final boolean broken;
    private final String errorMessage;
    private final List<String> headings;
    private final List<String> links;

    private ParsedPage(String url, int depth, boolean broken, String errorMessage,
                       List<String> headings, List<String> links) {
        this.url = url;
        this.depth = depth;
        this.broken = broken;
        this.errorMessage = errorMessage;
        this.headings = headings;
        this.links = links;
    }

    public static ParsedPage successful(String url, int depth, List<String> headings, List<String> links) {
        return new ParsedPage(url, depth, false, null, headings, links);
    }

    public static ParsedPage broken(String url, int depth, String errorMessage) {
        return new ParsedPage(url, depth, true, errorMessage, List.of(), List.of());
    }

    public String getUrl() {
        return url;
    }

    public int getDepth() {
        return depth;
    }

    public boolean isBroken() {
        return broken;
    }

    /** Returns the fetch error message, or empty if the page was successfully crawled. */
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    public List<String> getHeadings() {
        return headings;
    }

    public List<String> getLinks() {
        return links;
    }
}
