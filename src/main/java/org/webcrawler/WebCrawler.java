package org.webcrawler;

import org.jsoup.nodes.Document;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Class where actual crawling happens.
 * Visits pages using BFS - breadth first approach (level by level),
 * extracts headings and links, and returns a list of results for the report.
 * Stays within the allowed domain and stops at the configured depth.
 * Queue Class is essential for scheduling here.
 */
public class WebCrawler {

    private final UserInput userInput;
    private final HTMLFetcher fetcher;
    private final PageParser parser;

    public WebCrawler(UserInput userInput, HTMLFetcher fetcher, PageParser parser) {
        this.userInput = userInput;
        this.fetcher = fetcher;
        this.parser = parser;
    }

    public List<ParsedPage> crawl() {
        List<ParsedPage> results = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Queue<CrawlTask> queue = new ArrayDeque<>();

        String startUrl = UrlUtils.normalizeUrl(userInput.getStartUrl());
        queue.add(new CrawlTask(startUrl, 0));

        while (!queue.isEmpty()) {
            CrawlTask task = queue.poll();
            String url = task.url();
            int depth = task.depth();

            if (visited.contains(url) || depth > userInput.getMaxDepth()) {
                continue;
            }
            visited.add(url);

            Document doc = fetcher.fetch(url);
            if (doc == null) {
                results.add(new ParsedPage(url, depth, true, List.of(), List.of()));
                continue;
            }

            List<String> headings = parser.extractHeadings(doc);
            List<String> links = parser.extractLinks(doc);

            results.add(new ParsedPage(url, depth, false, headings, links));

            for (String link : links) {
                String normalized = UrlUtils.normalizeUrl(link);
                if (!visited.contains(normalized)
                        && UrlUtils.domainMatches(normalized, userInput.getDomain())) {
                    queue.add(new CrawlTask(normalized, depth + 1));
                }
            }
        }

        return results;
    }

    //Helper to hold a URL + its crawl depth in the queue
    private record CrawlTask(String url, int depth) {}
}
