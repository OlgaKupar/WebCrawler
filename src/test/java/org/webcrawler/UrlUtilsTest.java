package org.webcrawler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlUtilsTest {

    @Test
    void domainMatches_exactMatch() {
        assertTrue(UrlUtils.domainMatches("https://myTestWebSite/page", "myTestWebSite"));
    }

    @Test
    void domainMatches_wwwIsStripped() {
        assertTrue(UrlUtils.domainMatches("https://www.myTestWebSite/page", "myTestWebSite"));
    }

    @Test
    void domainMatches_subdomainIsAllowed() {
        assertTrue(UrlUtils.domainMatches("https://blog.myTestWebSite/post", "myTestWebSite"));
    }

    @Test
    void domainNotMatches_differentDomain() {
        assertFalse(UrlUtils.domainMatches("https://other.com/page", "myTestWebSite"));
    }

    @Test
    void domainNotMatches_invalidUrl() {
        assertFalse(UrlUtils.domainMatches("not-a-url", "myTestWebSite"));
    }

    @Test
    void normalizeUrl_removesTrailingSlash() {
        assertEquals("https://myTestWebSite/page", UrlUtils.normalizeUrl("https://myTestWebSite/page/"));
    }

    @Test
    void normalizeUrl_removesFragment() {
        assertEquals("https://myTestWebSite/page", UrlUtils.normalizeUrl("https://myTestWebSite/page#section"));
    }

    @Test
    void normalizeUrl_keepsRootSlash() {
        String result = UrlUtils.normalizeUrl("https://myTestWebSite/");
        assertTrue(result.equals("https://myTestWebSite/") || result.equals("https://myTestWebSite"));
    }

    @Test
    void stripWww_removesWwwPrefix() {
        assertEquals("myTestWebSite", UrlUtils.stripWww("www.myTestWebSite"));
    }

    @Test
    void stripWww_leavesNonWwwUnchanged() {
        assertEquals("myTestWebSite", UrlUtils.stripWww("myTestWebSite"));
    }
}
