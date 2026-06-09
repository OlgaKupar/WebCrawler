package org.webcrawler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsoupContentParserTest {

    private JsoupContentParser parser;

    @BeforeEach
    void setUp() {
        parser = new JsoupContentParser();
    }

    @ParameterizedTest
    @CsvSource({
        "h1, First,  '#'",
        "h2, Second, '##'",
        "h3, Third,  '###'",
        "h4, Fourth, '####'",
        "h5, Fifth,  '#####'",
        "h6, Sixth,  '######'"
    })
    void extractHeadings_formatsEachLevel(String tag, String text, String prefix) {
        FetchedPage page = new FetchedPage("<" + tag + ">" + text + "</" + tag + ">", "https://test.com");
        List<String> headings = parser.extractHeadings(page);
        assertEquals(prefix + " " + text, headings.get(0));
    }

    @Test
    void extractHeadings_returnsOneEntryPerHeading() {
        FetchedPage page = new FetchedPage(
                "<h1>A</h1><h2>B</h2><h3>C</h3><h4>D</h4><h5>E</h5><h6>F</h6>",
                "https://test.com"
        );
        assertEquals(6, parser.extractHeadings(page).size());
    }

    @Test
    void extractHeadings_emptyWhenNoHeadings() {
        FetchedPage page = new FetchedPage("<p>No headings here</p>", "https://test.com");
        assertTrue(parser.extractHeadings(page).isEmpty());
    }

    @Test
    void extractLinks_returnsAbsoluteHttpLinks() {
        FetchedPage page = new FetchedPage(
                "<a href=\"https://myTestWebSite\">Link</a>" +
                "<a href=\"https://other.com\">Other</a>",
                "https://start.com"
        );
        List<String> links = parser.extractLinks(page);
        assertEquals(2, links.size());
        assertTrue(links.contains("https://myTestWebSite"));
        assertTrue(links.contains("https://other.com"));
    }

    @Test
    void extractLinks_resolvesRelativeLinks() {
        FetchedPage page = new FetchedPage("<a href=\"/about\">About</a>", "https://myTestWebSite");
        List<String> links = parser.extractLinks(page);
        assertEquals(1, links.size());
        assertEquals("https://myTestWebSite/about", links.get(0));
    }

    @Test
    void extractLinks_ignoresMailtoLinks() {
        FetchedPage page = new FetchedPage("<a href=\"mailto:a@b.com\">Mail</a>", "https://myTestWebSite");
        assertTrue(parser.extractLinks(page).isEmpty());
    }

    @Test
    void extractLinks_ignoresJavascriptLinks() {
        FetchedPage page = new FetchedPage("<a href=\"javascript:void(0)\">JS</a>", "https://myTestWebSite");
        assertTrue(parser.extractLinks(page).isEmpty());
    }
}
