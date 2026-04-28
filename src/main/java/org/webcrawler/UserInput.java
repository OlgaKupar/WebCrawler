package org.webcrawler;

/**
 * Data Class, holding parameters used to configure webcrawler.
 * Input source - console.
 * Example: https://myWebSite.at 3 myWebSite.at
 */
public class UserInput {

    private final String startUrl;
    private final int maxDepth;
    private final String domain;

    public UserInput(String startUrl, int maxDepth, String domain) {
        this.startUrl = startUrl;
        this.maxDepth = maxDepth;
        this.domain = domain;
    }

    public String getStartUrl() {
        return startUrl;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public String getDomain() {
        return domain;
    }
}
