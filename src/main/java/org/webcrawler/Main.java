package org.webcrawler;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("At least 3 arguments are required.");
            System.err.println("Usage: java -jar webcrawler.jar <url> <depth> <domain>");
            System.exit(1);
        }

        String url = args[0];
        int depth = checkDepth(args[1]);
        String domain = args[2];

        UserInput userInput = new UserInput(url, depth, domain);
        WebCrawler crawler = new WebCrawler(userInput, new HTMLFetcher(), new PageParser());

        System.out.println("Crawling " + url + " (max depth: " + depth + ", domain: " + domain + ")...");
        List<ParsedPage> results = crawler.crawl();

        String outputFile = "Report.md";
        new MarkdownReportWriter().write(results, outputFile);
        System.out.println("Finished crawling. Report saved to: " + outputFile);
    }

    private static int checkDepth(String value) {
        try {
            int depth = Integer.parseInt(value);
            if (depth < 0) {
                System.err.println("Depth must be 0 or greater.");
                System.exit(1);
            }
            return depth;
        } catch (NumberFormatException e) {
            System.err.println("Depth must be an integer, got: " + value);
            System.exit(1);
            return 0;
        }
    }
}
