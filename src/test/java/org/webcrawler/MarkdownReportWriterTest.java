package org.webcrawler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownReportWriterTest {

    @TempDir
    Path tempDir;

    private Path output;
    private MarkdownReportWriter writer;

    @BeforeEach
    void setUp() {
        output = tempDir.resolve("report.md");
        writer = new MarkdownReportWriter();
    }

    private String writeAndRead(List<ParsedPage> pages) throws IOException {
        writer.write(pages, output.toString());
        return Files.readString(output);
    }

    @Test
    void write_createsFileWithReportHeader() throws IOException {
        String content = writeAndRead(List.of());
        assertTrue(content.contains("# Web Crawler Report"));
    }

    @Test
    void write_includesPageUrl() throws IOException {
        ParsedPage page = ParsedPage.successful("https://myTestWebSite.at", 0, List.of(), List.of());
        String content = writeAndRead(List.of(page));
        assertTrue(content.contains("https://myTestWebSite.at"));
    }

    @Test
    void write_includesExtractedHeadings() throws IOException {
        ParsedPage page = ParsedPage.successful("https://myTestWebSite.at", 0, List.of("# Hello", "## World"), List.of());
        String content = writeAndRead(List.of(page));
        assertTrue(content.contains("# Hello"));
        assertTrue(content.contains("## World"));
    }

    @Test
    void write_marksBrokenLinks() throws IOException {
        ParsedPage broken = ParsedPage.broken("https://myTestWebSite.at/dead", 1, "HTTP 404");
        String content = writeAndRead(List.of(broken));
        assertTrue(content.contains("broken link"));
        assertTrue(content.contains("https://myTestWebSite.at/dead"));
    }

    @Test
    void write_includesErrorMessageForBrokenPage() throws IOException {
        ParsedPage broken = ParsedPage.broken("https://myTestWebSite.at/gone", 0, "HTTP 410");
        String content = writeAndRead(List.of(broken));
        assertTrue(content.contains("HTTP 410"));
    }

    @Test
    void write_indentsChildPagesByDepth() throws IOException {
        ParsedPage root  = ParsedPage.successful("https://myTestWebSite.at",       0, List.of(), List.of());
        ParsedPage child = ParsedPage.successful("https://myTestWebSite.at/about", 1, List.of(), List.of());
        writer.write(List.of(root, child), output.toString());

        List<String> lines = Files.readAllLines(output);
        String rootLine  = lines.stream().filter(l -> l.contains("myTestWebSite.at") && !l.contains("about")).findFirst().orElse("");
        String childLine = lines.stream().filter(l -> l.contains("about")).findFirst().orElse("");

        assertTrue(countLeadingSpaces(childLine) > countLeadingSpaces(rootLine));
    }

    @Test
    void write_emitsNewDepthHeaderWhenDepthOrderIsNonMonotonic() throws IOException {
        ParsedPage depth0      = ParsedPage.successful("https://myTestWebSite.at",   0, List.of(), List.of());
        ParsedPage depth1      = ParsedPage.successful("https://myTestWebSite.at/a", 1, List.of(), List.of());
        ParsedPage depth0Again = ParsedPage.successful("https://myTestWebSite.at/b", 0, List.of(), List.of());
        String content = writeAndRead(List.of(depth0, depth1, depth0Again));
        assertTrue(content.contains("https://myTestWebSite.at/b"));
        assertEquals(2, countOccurrences(content, "## Crawling at depth 0"));
    }

    @Test
    void write_printsDepthHeaderOncePerLevel() throws IOException {
        ParsedPage pageA = ParsedPage.successful("https://myTestWebSite.at",         0, List.of(), List.of());
        ParsedPage pageB = ParsedPage.successful("https://myTestWebSite.at/about",   1, List.of(), List.of());
        ParsedPage pageC = ParsedPage.successful("https://myTestWebSite.at/contact", 1, List.of(), List.of());
        String content = writeAndRead(List.of(pageA, pageB, pageC));
        assertEquals(1, countOccurrences(content, "## Crawling at depth 0"));
        assertEquals(1, countOccurrences(content, "## Crawling at depth 1"));
    }

    private int countLeadingSpaces(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') count++;
            else break;
        }
        return count;
    }

    private int countOccurrences(String text, String target) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(target, index)) != -1) {
            count++;
            index += target.length();
        }
        return count;
    }
}
