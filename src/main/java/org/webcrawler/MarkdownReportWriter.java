package org.webcrawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Class to generate a Report in MarkDown Format with all Crawled Pages in desired format.
 */
public class MarkdownReportWriter {

    public void write(List<ParsedPage> results, String outputPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("# Web Crawler Report");
            writer.newLine();
            writer.write("_Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "_");
            writer.newLine();

            int currentDepth = -1;

            for (ParsedPage result : results) {
                int depth = result.getDepth();
                String indent = "  ".repeat(depth);

                if (depth != currentDepth) {
                    currentDepth = depth;
                    writer.newLine();
                    writer.write("## Crawling at depth " + depth);
                    writer.newLine();
                    writer.newLine();
                }

                if (result.isBroken()) {
                    writer.write(indent + "- **broken link** " + result.getUrl());
                    writer.newLine();
                    continue;
                }

                writer.write(indent + "- **" + result.getUrl() + "**");
                writer.newLine();

                for (String heading : result.getHeadings()) {
                    writer.write(indent + "  " + heading);
                    writer.newLine();
                }

                if (!result.getLinks().isEmpty()) {
                    writer.write(indent + "  Links:");
                    writer.newLine();
                    for (String link : result.getLinks()) {
                        writer.write(indent + "    - " + link);
                        writer.newLine();
                    }
                }

                writer.newLine();
            }
        }
    }
}
