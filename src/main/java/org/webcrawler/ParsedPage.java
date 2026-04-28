package org.webcrawler;

import java.util.List;

/**
 * Data Class to store the crawl result for a single web page — its URL, depth level,
 * whether it was reachable, and the headings and links extracted from it.
 */
public class ParsedPage {

    private final String url;
    private final int depth;
    private final boolean broken;
    private final List<String> headings;
    private final List<String> links;

    public ParsedPage(String url, int depth, boolean broken, List<String> headings, List<String> links) {
        this.url = url;
        this.depth = depth;
        this.broken = broken;
        this.headings = headings;
        this.links = links;
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

    public List<String> getHeadings() {
        return headings;
    }

    public List<String> getLinks() {
        return links;
    }
}
