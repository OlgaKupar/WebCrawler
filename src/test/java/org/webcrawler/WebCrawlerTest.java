package org.webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebCrawlerTest {

    private WebCrawler buildCrawler(UserInput userInput, HTMLFetcher fetcher) {
        return new WebCrawler(userInput, fetcher, new PageParser());
    }

    @Test
    void crawl_returnsSingleResultForStartPage() {
        HTMLFetcher fetcher = mock(HTMLFetcher.class);
        Document doc = Jsoup.parse("<h1>Hello</h1>", "https://myTestWebSite.at");
        when(fetcher.fetch("https://myTestWebSite.at")).thenReturn(doc);

        UserInput config = new UserInput("https://myTestWebSite.at", 0, "myTestWebSite.at");
        List<ParsedPage> results = buildCrawler(config, fetcher).crawl();

        assertEquals(1, results.size());
        assertEquals("https://myTestWebSite.at", results.get(0).getUrl());
        assertFalse(results.get(0).isBroken());
        assertEquals(List.of("# Hello"), results.get(0).getHeadings());
    }

    @Test
    void crawl_marksBrokenLinkWhenFetchFails() {
        HTMLFetcher fetcher = mock(HTMLFetcher.class);
        when(fetcher.fetch(anyString())).thenReturn(null);

        UserInput config = new UserInput("https://myTestWebSite.at", 1, "myTestWebSite.at");
        List<ParsedPage> results = buildCrawler(config, fetcher).crawl();

        assertEquals(1, results.size());
        assertTrue(results.get(0).isBroken());
    }

    @Test
    void crawl_doesNotVisitSameUrlTwice() {
        HTMLFetcher fetcher = mock(HTMLFetcher.class);
        Document doc = Jsoup.parse(
                "<h1>A</h1><a href=\"https://myTestWebSite.at\">self</a>",
                "https://myTestWebSite.at"
        );
        when(fetcher.fetch("https://myTestWebSite.at")).thenReturn(doc);

        UserInput config = new UserInput("https://myTestWebSite.at", 3, "myTestWebSite.at");
        List<ParsedPage> results = buildCrawler(config, fetcher).crawl();

        assertEquals(1, results.size());
        verify(fetcher, times(1)).fetch("https://myTestWebSite.at");
    }

    @Test
    void crawl_respectsMaxDepth() {
        HTMLFetcher fetcher = mock(HTMLFetcher.class);

        Document root  = Jsoup.parse("<a href=\"https://myTestWebSite.at/p1\">P1</a>", "https://myTestWebSite.at");
        Document page1 = Jsoup.parse("<a href=\"https://myTestWebSite.at/p2\">P2</a>", "https://myTestWebSite.at/p1");

        when(fetcher.fetch("https://myTestWebSite.at")).thenReturn(root);
        when(fetcher.fetch("https://myTestWebSite.at/p1")).thenReturn(page1);

        UserInput config = new UserInput("https://myTestWebSite.at", 1, "myTestWebSite.at");
        List<ParsedPage> results = buildCrawler(config, fetcher).crawl();

        assertEquals(2, results.size());
        verify(fetcher, never()).fetch("https://myTestWebSite.at/p2");
    }

    @Test
    void crawl_skipsLinksOutsideAllowedDomain() {
        HTMLFetcher fetcher = mock(HTMLFetcher.class);
        Document doc = Jsoup.parse(
                "<a href=\"https://other.com/page\">External</a>",
                "https://myTestWebSite.at"
        );
        when(fetcher.fetch("https://myTestWebSite.at")).thenReturn(doc);

        UserInput config = new UserInput("https://myTestWebSite.at", 2, "myTestWebSite.at");
        List<ParsedPage> results = buildCrawler(config, fetcher).crawl();

        assertEquals(1, results.size());
        verify(fetcher, never()).fetch("https://other.com/page");
    }
}
