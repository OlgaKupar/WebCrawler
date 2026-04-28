package org.webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageParserTest {

    private PageParser parser;

    @BeforeEach
    void setUp() {
        parser = new PageParser();
    }

    @Test
    void extractHeadings_returnsAllLevels() {
        Document doc = Jsoup.parse(
                "<h1>First</h1><h2>Second</h2><h3>Third</h3>" +
                "<h4>Fourth</h4><h5>Fifth</h5><h6>Sixth</h6>"
        );
        List<String> headings = parser.extractHeadings(doc);
        assertEquals(6, headings.size());
        assertEquals("# First",    headings.get(0));
        assertEquals("## Second",  headings.get(1));
        assertEquals("### Third",  headings.get(2));
        assertEquals("#### Fourth", headings.get(3));
        assertEquals("##### Fifth", headings.get(4));
        assertEquals("###### Sixth", headings.get(5));
    }

    @Test
    void extractHeadings_emptyWhenNoHeadings() {
        Document doc = Jsoup.parse("<p>Empty</p>");
        assertTrue(parser.extractHeadings(doc).isEmpty());
    }

    @Test
    void extractLinks_returnsAbsoluteHttpLinks() {
        Document doc = Jsoup.parse(
                "<a href=\"https://myTestWebSite\">Link</a>" +
                "<a href=\"https://other.com\">Other</a>",
                "https://start.com"
        );
        List<String> links = parser.extractLinks(doc);
        assertEquals(2, links.size());
        assertTrue(links.contains("https://myTestWebSite"));
        assertTrue(links.contains("https://other.com"));
    }

    @Test
    void extractLinks_resolvesRelativeLinks() {
        Document doc = Jsoup.parse("<a href=\"/about\">About</a>", "https://myTestWebSite");
        List<String> links = parser.extractLinks(doc);
        assertEquals(1, links.size());
        assertEquals("https://myTestWebSite/about", links.get(0));
    }

    @Test
    void extractLinks_ignoresMailtoAndJavascript() {
        Document doc = Jsoup.parse(
                "<a href=\"mailto:a@b.com\">Mail</a>" +
                "<a href=\"javascript:void(0)\">JS</a>",
                "https://myTestWebSite"
        );
        assertTrue(parser.extractLinks(doc).isEmpty());
    }
}
