package org.webcrawler;

import java.util.List;

public interface ContentParser {

    // returns headings as Markdown strings, e.g. "## Title"
    List<String> extractHeadings(FetchedPage page);

    List<String> extractLinks(FetchedPage page);
}
