package org.webcrawler;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("At least 3 arguments are required.");
            System.err.println("Usage: java -jar webcrawler.jar <url> <depth> <domain> [domain2...]");
            System.exit(1);
            return;
        }

        String url = args[0];
        int depth;
        try {
            depth = parseDepth(args[1]);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }
        List<String> domains = Arrays.stream(args, 2, args.length).toList();

        UserInput userInput = new UserInput(url, depth, domains);

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            WebCrawler crawler = new WebCrawler(userInput, new JsoupPageFetcher(), new JsoupContentParser(), executor);

            System.out.println("Crawling " + url + " (max depth: " + depth + ", domains: " + String.join(", ", domains) + ")...");
            List<ParsedPage> results = crawler.crawl();

            String outputFile = "Report.md";
            new MarkdownReportWriter().write(results, outputFile);
            System.out.println("Finished crawling. Report saved to: " + outputFile);
        } finally {
            executor.shutdown();
        }
    }

    private static int parseDepth(String value) {
        try {
            int depth = Integer.parseInt(value);
            if (depth < 0) throw new IllegalArgumentException("Depth must be 0 or greater.");
            return depth;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Depth must be an integer, got: " + value);
        }
    }
}
