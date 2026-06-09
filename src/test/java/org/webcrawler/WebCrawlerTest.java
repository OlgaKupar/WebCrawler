package org.webcrawler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebCrawlerTest {

    private static final String START_URL = "https://myTestWebSite.at";

    @Mock
    private PageFetcher fetcher;

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void tearDown() {
        executor.shutdown();
    }

    private WebCrawler buildCrawler(UserInput userInput) {
        return new WebCrawler(userInput, fetcher, new JsoupContentParser(), executor);
    }

    private UserInput userInput(int depth) {
        return new UserInput(START_URL, depth, List.of("myTestWebSite.at"));
    }

    private UserInput userInput(int depth, List<String> domains) {
        return new UserInput(START_URL, depth, domains);
    }

    @Test
    void crawl_returnsSingleResultForStartPage() {
        when(fetcher.fetch(START_URL))
                .thenReturn(new FetchedPage("<h1>Hello</h1>", START_URL));

        List<ParsedPage> results = buildCrawler(userInput(0)).crawl();

        assertEquals(1, results.size());
    }

    @Test
    void crawl_extractsHeadingsFromFetchedPage() {
        when(fetcher.fetch(START_URL))
                .thenReturn(new FetchedPage("<h1>Hello</h1>", START_URL));

        ParsedPage result = buildCrawler(userInput(0)).crawl().get(0);

        assertFalse(result.isBroken());
        assertEquals(List.of("# Hello"), result.getHeadings());
    }

    @Test
    void crawl_marksBrokenLinkWhenFetchFails() {
        when(fetcher.fetch(anyString())).thenThrow(new PageFetchException("HTTP 404"));

        List<ParsedPage> results = buildCrawler(userInput(1)).crawl();

        assertEquals(1, results.size());
        assertTrue(results.get(0).isBroken());
        assertEquals("HTTP 404", results.get(0).getErrorMessage().orElse(""));
    }

    @Test
    void crawl_doesNotVisitSameUrlTwice() {
        when(fetcher.fetch(START_URL))
                .thenReturn(new FetchedPage(
                        "<h1>A</h1><a href=\"" + START_URL + "\">self</a>",
                        START_URL
                ));

        List<ParsedPage> results = buildCrawler(userInput(3)).crawl();

        assertEquals(1, results.size());
        verify(fetcher, times(1)).fetch(START_URL);
    }

    @Test
    void crawl_respectsMaxDepth() {
        when(fetcher.fetch(START_URL))
                .thenReturn(new FetchedPage(
                        "<a href=\"https://myTestWebSite.at/p1\">P1</a>",
                        START_URL
                ));
        when(fetcher.fetch("https://myTestWebSite.at/p1"))
                .thenReturn(new FetchedPage(
                        "<a href=\"https://myTestWebSite.at/p2\">P2</a>",
                        "https://myTestWebSite.at/p1"
                ));

        List<ParsedPage> results = buildCrawler(userInput(1)).crawl();

        assertEquals(2, results.size());
        verify(fetcher, never()).fetch("https://myTestWebSite.at/p2");
    }

    @Test
    void crawl_skipsLinksOutsideAllowedDomain() {
        when(fetcher.fetch(START_URL))
                .thenReturn(new FetchedPage(
                        "<a href=\"https://other.com/page\">External</a>",
                        START_URL
                ));

        List<ParsedPage> results = buildCrawler(userInput(2)).crawl();

        assertEquals(1, results.size());
        verify(fetcher, never()).fetch("https://other.com/page");
    }

    @Test
    void crawl_followsLinksMatchingAnyAllowedDomain() {
        when(fetcher.fetch(START_URL))
                .thenReturn(new FetchedPage(
                        "<a href=\"https://other.org/page\">Other allowed</a>",
                        START_URL
                ));
        when(fetcher.fetch("https://other.org/page"))
                .thenReturn(new FetchedPage("<h1>Other</h1>", "https://other.org/page"));

        List<ParsedPage> results = buildCrawler(userInput(1, List.of("myTestWebSite.at", "other.org"))).crawl();

        assertEquals(2, results.size());
        verify(fetcher).fetch("https://other.org/page");
    }
}
