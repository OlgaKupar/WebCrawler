# WebCrawler

Crawls websites breadth-first and writes a Markdown report. Broken links show up in the
report with the error message.

## Requirements

- Java 21+
- Maven 3.8+

## Build

```bash
mvn package -DskipTests
```

JAR is at `target/webcrawler.jar`.

## Run

```bash
java -jar target/webcrawler.jar <url> <depth> <domain> [domain2 ...]
```

| Argument  | Description                                       |
|-----------|---------------------------------------------------|
| `url`     | Start URL (e.g. `https://example.com`)            |
| `depth`   | How deep to crawl (0 = start page only)           |
| `domain`  | At least one allowed domain (e.g. `example.com`)  |
| `domain2` | More allowed domains (optional)                   |

```bash
java -jar target/webcrawler.jar https://example.com 2 example.com
```

Report is written to `report.md` in the current directory.

## Test

```bash
mvn test
```

## Design

### Boundaries

We use jsoup for HTTP requests and HTML parsing. To avoid jsoup calls spreading across
the whole codebase, all jsoup code is limited to two classes: `JsoupPageFetcher` and
`JsoupContentParser`. Everything else only uses our own interfaces (`PageFetcher`,
`ContentParser`, `ReportWriter`). If we ever switch away from jsoup, only those two
files need to change.

### Single Responsibility

Each class has one job:

- `JsoupPageFetcher` — HTTP request only
- `JsoupContentParser` — extract headings and links from HTML
- `WebCrawler` — BFS crawl loop
- `MarkdownReportWriter` — write the report
- `Main` — wire everything together and start

`WebCrawler` is a compromise. It handles BFS, domain filtering, and concurrency in one
class, which is more than one responsibility. Splitting it into separate classes would
add complexity without much benefit at this size, so we kept it together. At least at
the method level each method does one thing: `submitAll` sends URLs to the thread pool,
`collectResults` waits for the results and handles errors.

### Error Handling

When a page fails (404, timeout, network error), `JsoupPageFetcher` throws a
`PageFetchException`. This keeps jsoup exceptions out of the rest of the code.
`WebCrawler` catches it and stores the message in `ParsedPage.broken(...)`. The report
shows it like this:

```
- **broken link** https://example.com/missing → HTTP 404
```

`ParsedPage` uses factory methods (`successful(...)` / `broken(...)`) instead of a
plain constructor so it is obvious which kind of result you are creating.

### Concurrency

All URLs at the same depth level are fetched in parallel. The main thread waits for all
of them before moving to the next level.

```
depth 0:  [url-A]               → parallel fetch → done
depth 1:  [url-B, url-C, url-D] → parallel fetch → done
depth 2:  [url-E, url-F]        → parallel fetch → done
```

Threads do not share data — each one works on a single URL and returns a `ParsedPage`.
The visited-URL set is only touched by the main thread between levels, so no
`synchronized` is needed. The `ExecutorService` is created in `Main` and injected into
`WebCrawler`, which makes it easy to use a single-thread executor in tests.

### Class overview

| Class | Role |
|-------|------|
| `PageFetcher` | Interface: fetch a page |
| `ContentParser` | Interface: extract headings and links |
| `ReportWriter` | Interface: write the report |
| `JsoupPageFetcher` | `PageFetcher` using jsoup |
| `JsoupContentParser` | `ContentParser` using jsoup |
| `PageFetchException` | Wraps all fetch errors |
| `FetchedPage` | Raw page data, no jsoup types |
| `ParsedPage` | One crawled URL: headings, links, optional error |
| `WebCrawler` | BFS crawl loop with concurrency |
| `MarkdownReportWriter` | Writes `Report.md` |
| `UrlUtils` | URL helpers (normalize, domain check) |
| `Main` | Entry point, wires everything together |
