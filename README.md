# WebCrawler

A simple web crawler written in Java that visits a website and its linked pages, extracts headings and links, and generates a compact Markdown report.

---

## What it does

Starting from a given URL, the crawler visits pages level by level (BFS), stays within the specified domain, and stops at the configured depth. For each page it records all headings (h1–h6) and links found. Broken links (unreachable pages or HTTP 4xx/5xx errors) are marked in the report and also printed to the console so you can spot them immediately.

---

## Classes

| Class | Role |
|---|---|
| `Main` | Entry point, reads and validates CLI arguments |
| `UserInput` | Holds the three input parameters: URL, depth, domain |
| `WebCrawler` | Core crawl loop — BFS traversal, tracks visited pages |
| `HTMLFetcher` | Fetches a page using jsoup, checks HTTP status |
| `PageParser` | Extracts headings and links from a fetched document |
| `UrlUtils` | URL normalization and domain matching helpers |
| `ParsedPage` | Data class — stores result for one crawled page |
| `MarkdownReportWriter` | Writes the final report.md file |

---

## Build

Requires Java 21+ and Maven.

```bash
mvn package
```

This compiles the code, runs all tests, and produces `target/webcrawler.jar`.

---

## Run

```bash
java -jar target/webcrawler.jar <url> <depth> <domain>
```

**Example:**
```bash
java -jar target/webcrawler.jar https://proagent-software.at 2 proagent-software.at
```

- `url` — the starting page
- `depth` — how many link levels deep to crawl (0 = start page only)
- `domain` — only pages on this domain (and subdomains) will be followed

The report is saved as `report.md` in the current directory.

---

## Test

```bash
mvn test
```

Runs all 30 unit tests covering crawl logic, HTML parsing, URL utilities, report writing, and HTTP status detection.

---

## Example report output

```markdown
# Web Crawler Report
_Generated: 2026-04-28 14:00:00_

## Crawling at depth 0

- **https://proagent-software.at**
  # Welcome
  ## Our Services
  Links:
    - https://proagent-software.at/about
    - https://proagent-software.at/contact

## Crawling at depth 1

  - **https://proagent-software.at/about**
    ## About Us
  
  - **broken link** https://proagent-software.at/contact
```

