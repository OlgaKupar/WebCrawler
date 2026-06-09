package org.webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class JsoupContentParser implements ContentParser {

    @Override
    public List<String> extractHeadings(FetchedPage page) {
        Document doc = parseDocument(page);
        List<String> headings = new ArrayList<>();
        Elements elements = doc.select("h1, h2, h3, h4, h5, h6");
        for (Element element : elements) {
            int level = Integer.parseInt(element.tagName().substring(1));
            headings.add("#".repeat(level) + " " + element.text().trim());
        }
        return headings;
    }

    @Override
    public List<String> extractLinks(FetchedPage page) {
        Document doc = parseDocument(page);
        List<String> links = new ArrayList<>();
        Elements anchors = doc.select("a[href]");
        for (Element anchor : anchors) {
            String absoluteUrl = anchor.absUrl("href");
            if (!absoluteUrl.isBlank() && (absoluteUrl.startsWith("http://") || absoluteUrl.startsWith("https://"))) {
                links.add(absoluteUrl);
            }
        }
        return links;
    }

    private Document parseDocument(FetchedPage page) {
        return Jsoup.parse(page.html(), page.baseUrl());
    }
}
