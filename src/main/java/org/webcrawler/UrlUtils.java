package org.webcrawler;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlUtils {

    private UrlUtils() {}

    // Strips "www." so that www.example.com and example.com are treated the same.
    public static boolean domainMatches(String url, String domain) {
        String host = extractHost(url);
        if (host == null) return false;
        String normalizedHost = stripWww(host.toLowerCase());
        String normalizedDomain = stripWww(domain.toLowerCase());
        return normalizedHost.equals(normalizedDomain) || normalizedHost.endsWith("." + normalizedDomain);
    }

    // Strips trailing slashes and fragments to prevent crawling the same page twice.
    public static String normalizeUrl(String url) {
        try {
            URI uri = new URI(url).normalize();
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
            return url;
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
