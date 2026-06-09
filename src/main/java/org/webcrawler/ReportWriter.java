package org.webcrawler;

import java.io.IOException;
import java.util.List;

public interface ReportWriter {

    void write(List<ParsedPage> results, String outputPath) throws IOException;
}
