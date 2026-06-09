package org.webcrawler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WebCrawler {

    private static final int FETCH_TIMEOUT_SECONDS = 15;

    private final UserInput userInput;
    private final PageFetcher fetcher;
    private final ContentParser parser;
    private final ExecutorService executor;

    public WebCrawler(UserInput userInput, PageFetcher fetcher, ContentParser parser, ExecutorService executor) {
        this.userInput = userInput;
        this.fetcher = fetcher;
        this.parser = parser;
        this.executor = executor;
    }

    public List<ParsedPage> crawl() {
        List<ParsedPage> allResults = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        List<String> currentLevel = new ArrayList<>();
        String startUrl = UrlUtils.normalizeUrl(userInput.getStartUrl());
        currentLevel.add(startUrl);
        visited.add(startUrl);

        for (int depth = 0; depth <= userInput.getMaxDepth() && !currentLevel.isEmpty(); depth++) {
            List<ParsedPage> levelResults = processLevel(currentLevel, depth);
            allResults.addAll(levelResults);
            currentLevel = collectNextLevel(levelResults, visited);
        }

        return allResults;
    }

    private List<ParsedPage> processLevel(List<String> urls, int depth) {
        return collectResults(submitAll(urls, depth), urls, depth);
    }

    private List<Future<ParsedPage>> submitAll(List<String> urls, int depth) {
        return urls.stream()
                .map(url -> executor.submit(() -> processUrl(url, depth)))
                .toList();
    }

    private List<ParsedPage> collectResults(List<Future<ParsedPage>> futures, List<String> urls, int depth) {
        List<ParsedPage> results = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            try {
                results.add(futures.get(i).get(FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS));
            } catch (TimeoutException e) {
                futures.get(i).cancel(true);
                results.add(ParsedPage.broken(urls.get(i), depth, "Timed out"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                for (int j = i; j < urls.size(); j++) {
                    results.add(ParsedPage.broken(urls.get(j), depth, "Crawl interrupted"));
                }
                break;
            } catch (ExecutionException e) {
                results.add(ParsedPage.broken(urls.get(i), depth, "Task execution failed: " + e.getCause().getMessage()));
            }
        }
        return results;
    }

    private ParsedPage processUrl(String url, int depth) {
        try {
            FetchedPage fetched = fetcher.fetch(url);
            List<String> headings = parser.extractHeadings(fetched);
            List<String> links = parser.extractLinks(fetched);
            return ParsedPage.successful(url, depth, headings, links);
        } catch (PageFetchException e) {
            return ParsedPage.broken(url, depth, e.getMessage());
        } catch (RuntimeException e) {
            return ParsedPage.broken(url, depth, "Unexpected error: " + e.getMessage());
        }
    }

    private List<String> collectNextLevel(List<ParsedPage> levelResults, Set<String> visited) {
        List<String> nextLevel = new ArrayList<>();
        for (ParsedPage page : levelResults) {
            if (page.isBroken()) continue;
            for (String link : page.getLinks()) {
                String normalized = UrlUtils.normalizeUrl(link);
                if (matchesAllowedDomain(normalized) && visited.add(normalized)) {
                    nextLevel.add(normalized);
                }
            }
        }
        return nextLevel;
    }

    private boolean matchesAllowedDomain(String url) {
        return userInput.getDomains().stream().anyMatch(d -> UrlUtils.domainMatches(url, d));
    }
}
