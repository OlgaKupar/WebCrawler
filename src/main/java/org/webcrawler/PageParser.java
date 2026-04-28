package org.webcrawler;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper Class to JSOUP Library.
 * Isolates methods for extracting of Headings (h1 - h6) and absolute links ( a[href]).
 */
public class PageParser {

    /**
     * Returns headings in the format "### text" where the number of #
     * characters matches the heading level (h1 → #, h2 → ##, ...).
     */
    public List<String> extractHeadings(Document doc) {
        List<String> headings = new ArrayList<>();
        Elements elements = doc.select("h1, h2, h3, h4, h5, h6");
        for (Element element : elements) {
            int level = Integer.parseInt(element.tagName().substring(1));
            String prefix = "#".repeat(level);
            headings.add(prefix + " " + element.text().trim());
        }
        return headings;
    }

    /**
     * Returns all absolute URLs found in anchor tags (href=...) on the page.
     * Relative URLs are resolved against the page's base URL by jsoup.
     */
    public List<String> extractLinks(Document doc) {
        List<String> links = new ArrayList<>();
        Elements anchors = doc.select("a[href]");
        for (Element anchor : anchors) {
            String absoluteURL = anchor.absUrl("href");
            if (!absoluteURL.isBlank() && (absoluteURL.startsWith("http://") || absoluteURL.startsWith("https://"))) {
                links.add(absoluteURL);
            }
        }
        return links;
    }
}
