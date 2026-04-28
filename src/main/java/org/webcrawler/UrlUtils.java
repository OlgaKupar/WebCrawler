package org.webcrawler;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlUtils {

    private UrlUtils() {}

    /**
     * Returns true if the host of the given URL matches the allowed domain.
     * Strips leading "www." before comparing so that "www.example.com"
     * and "example.com" are treated as the same domain.
     */
    public static boolean domainMatches(String inputUrl, String inputDomain) {
        String host = extractHost(inputUrl);
        if (host == null) return false;
        String normalizedHost = stripWww(host.toLowerCase());
        String normalizedDomain = stripWww(inputDomain.toLowerCase());
        return normalizedHost.equals(normalizedDomain) || normalizedHost.endsWith("." + normalizedDomain);
    }

    /**
     * Strips trailing slashes and fragments so that the same page is not
     * crawled twice under two different URL strings.
     */
    public static String normalizeUrl(String inputUrl) {
        try {
            URI uri = new URI(inputUrl).normalize();
            URI withoutFragment = new URI(
                    uri.getScheme(),
                    uri.getAuthority(),
                    uri.getPath(),
                    uri.getQuery(),
                    null
            );
            String result = withoutFragment.toString();
            // Remove trailing slash unless it is the root path
            if (result.endsWith("/") && !result.equals(uri.getScheme() + "://" + uri.getAuthority() + "/")) {
                result = result.substring(0, result.length() - 1);
            }
            return result;
        } catch (URISyntaxException e) {
            return inputUrl;
        }
    }

    public static String extractHost(String url) {
        try {
            return new URI(url).getHost();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    static String stripWww(String host) {
        return host.startsWith("www.") ? host.substring(4) : host;
    }
}
