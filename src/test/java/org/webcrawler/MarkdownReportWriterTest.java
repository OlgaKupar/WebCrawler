package org.webcrawler;

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

    @Test
    void write_createsFileWithReportHeader() throws IOException {
        Path output = tempDir.resolve("report.md");
        new MarkdownReportWriter().write(List.of(), output.toString());
        String content = Files.readString(output);
        assertTrue(content.contains("# Web Crawler Report"));
    }

    @Test
    void write_includesHeadingsAndUrl() throws IOException {
        Path output = tempDir.resolve("report.md");
        ParsedPage result = new ParsedPage(
                "https://myTestWebSite.at", 0, false,
                List.of("# Hello", "## World"),
                List.of()
        );
        new MarkdownReportWriter().write(List.of(result), output.toString());
        String content = Files.readString(output);
        assertTrue(content.contains("https://myTestWebSite.at"));
        assertTrue(content.contains("# Hello"));
        assertTrue(content.contains("## World"));
    }

    @Test
    void write_marksBrokenLinks() throws IOException {
        Path output = tempDir.resolve("report.md");
        ParsedPage broken = new ParsedPage(
                "https://myTestWebSite.at/dead", 1, true, List.of(), List.of()
        );
        new MarkdownReportWriter().write(List.of(broken), output.toString());
        String content = Files.readString(output);
        assertTrue(content.contains("broken link"));
        assertTrue(content.contains("https://myTestWebSite.at/dead"));
    }

    @Test
    void write_indentsChildPagesByDepth() throws IOException {
        Path output = tempDir.resolve("report.md");
        ParsedPage root  = new ParsedPage("https://myTestWebSite.at",       0, false, List.of(), List.of());
        ParsedPage child = new ParsedPage("https://myTestWebSite.at/about", 1, false, List.of(), List.of());
        new MarkdownReportWriter().write(List.of(root, child), output.toString());

        List<String> lines = Files.readAllLines(output);
        String rootLine  = lines.stream().filter(l -> l.contains("myTestWebSite.at") && !l.contains("about")).findFirst().orElse("");
        String childLine = lines.stream().filter(l -> l.contains("about")).findFirst().orElse("");

        int rootIndent  = countLeadingSpaces(rootLine);
        int childIndent = countLeadingSpaces(childLine);
        assertTrue(childIndent > rootIndent);
    }

    @Test
    void write_printsDepthHeaderOncePerLevel() throws IOException {
        Path output = tempDir.resolve("report.md");
        ParsedPage pageA = new ParsedPage("https://myTestWebSite.at",        0, false, List.of(), List.of());
        ParsedPage pageB = new ParsedPage("https://myTestWebSite.at/about",  1, false, List.of(), List.of());
        ParsedPage pageC = new ParsedPage("https://myTestWebSite.at/contact",1, false, List.of(), List.of());
        new MarkdownReportWriter().write(List.of(pageA, pageB, pageC), output.toString());

        String content = Files.readString(output);
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
