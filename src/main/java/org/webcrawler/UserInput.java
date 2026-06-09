package org.webcrawler;

import java.util.List;

// <url> <depth> <domain> [domain2 ...]
public class UserInput {

    private final String startUrl;
    private final int maxDepth;
    private final List<String> domains;

    public UserInput(String startUrl, int maxDepth, List<String> domains) {
        this.startUrl = startUrl;
        this.maxDepth = maxDepth;
        this.domains = domains;
    }

    public String getStartUrl() {
        return startUrl;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public List<String> getDomains() {
        return domains;
    }
}
