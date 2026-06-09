package org.webcrawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MarkdownReportWriter implements ReportWriter {

    @Override
    public void write(List<ParsedPage> results, String outputPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writeHeader(writer);
            int currentDepth = -1;
            for (ParsedPage page : results) {
                if (page.getDepth() != currentDepth) {
                    currentDepth = page.getDepth();
                    writeDepthSection(writer, currentDepth);
                }
                writePageEntry(writer, page);
            }
        }
    }

    private void writeHeader(BufferedWriter writer) throws IOException {
        writer.write("# Web Crawler Report");
        writer.newLine();
        writer.write("_Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "_");
        writer.newLine();
    }

    private void writeDepthSection(BufferedWriter writer, int depth) throws IOException {
        writer.newLine();
        writer.write("## Crawling at depth " + depth);
        writer.newLine();
        writer.newLine();
    }

    private void writePageEntry(BufferedWriter writer, ParsedPage page) throws IOException {
        String indent = "  ".repeat(page.getDepth());
        if (page.isBroken()) {
            writeBrokenEntry(writer, page, indent);
        } else {
            writeSuccessfulEntry(writer, page, indent);
        }
    }

    private void writeBrokenEntry(BufferedWriter writer, ParsedPage page, String indent) throws IOException {
        String error = page.getErrorMessage().map(msg -> " → " + msg).orElse("");
        writer.write(indent + "- **broken link** " + page.getUrl() + error);
        writer.newLine();
    }

    private void writeSuccessfulEntry(BufferedWriter writer, ParsedPage page, String indent) throws IOException {
        writer.write(indent + "- **" + page.getUrl() + "**");
        writer.newLine();
        if (!page.getHeadings().isEmpty() || !page.getLinks().isEmpty()) {
            writer.newLine();
        }
        writeHeadings(writer, page, indent);
        writeLinks(writer, page, indent);
        writer.newLine();
    }

    private void writeHeadings(BufferedWriter writer, ParsedPage page, String indent) throws IOException {
        for (String heading : page.getHeadings()) {
            writer.write(indent + "  " + heading);
            writer.newLine();
        }
    }

    private void writeLinks(BufferedWriter writer, ParsedPage page, String indent) throws IOException {
        if (page.getLinks().isEmpty()) return;
        writer.write(indent + "  Links:");
        writer.newLine();
        for (String link : page.getLinks()) {
            writer.write(indent + "    - " + link);
            writer.newLine();
        }
    }
}
