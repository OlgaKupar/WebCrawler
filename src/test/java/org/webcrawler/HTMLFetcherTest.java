package org.webcrawler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HTMLFetcherTest {

    private HTMLFetcher fetcher;

    @BeforeEach
    void setUp() {
        fetcher = new HTMLFetcher();
    }

    @Test
    void isBroken_returnsFalseFor200() {
        assertFalse(fetcher.isBroken(200));
    }

    @Test
    void isBroken_returnsFalseFor301Redirect() {
        assertFalse(fetcher.isBroken(301));
    }

    @Test
    void isBroken_returnsTrueFor404() {
        assertTrue(fetcher.isBroken(404));
    }

    @Test
    void isBroken_returnsTrueFor500() {
        assertTrue(fetcher.isBroken(500));
    }

    @Test
    void isBroken_returnsTrueForAny4xx() {
        assertTrue(fetcher.isBroken(400));
        assertTrue(fetcher.isBroken(403));
        assertTrue(fetcher.isBroken(410));
    }
}
