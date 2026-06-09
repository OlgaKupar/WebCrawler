package org.webcrawler;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
class JsoupPageFetcherTest {

    private JsoupPageFetcher fetcher;

    @BeforeEach
    void setUp() {
        fetcher = new JsoupPageFetcher();
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 301})
    void isBroken_returnsFalseForNonErrorCode(int statusCode) {
        assertFalse(fetcher.isBroken(statusCode));
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 403, 404, 410, 500})
    void isBroken_returnsTrueForStatusCode400OrAbove(int statusCode) {
        assertTrue(fetcher.isBroken(statusCode));
    }

    @Test
    void fetch_returnsFetchedPageForSuccessfulResponse(WireMockRuntimeInfo wm) {
        stubFor(get("/page")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withBody("<html><body><h1>Hello</h1></body></html>")));

        FetchedPage result = fetcher.fetch(wm.getHttpBaseUrl() + "/page");

        assertTrue(result.html().contains("Hello"));
        assertEquals(wm.getHttpBaseUrl() + "/page", result.baseUrl());
    }

    @Test
    void fetch_throwsPageFetchExceptionForBrokenPage(WireMockRuntimeInfo wm) {
        stubFor(get("/broken")
                .willReturn(aResponse().withStatus(404)));

        PageFetchException ex = assertThrows(PageFetchException.class,
                () -> fetcher.fetch(wm.getHttpBaseUrl() + "/broken"));

        assertTrue(ex.getMessage().contains("404"));
    }

    @Test
    void fetch_throwsPageFetchExceptionOnNetworkError() {
        assertThrows(PageFetchException.class,
                () -> fetcher.fetch("http://localhost:1/unreachable"));
    }
}
