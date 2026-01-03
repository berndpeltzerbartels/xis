package one.xis.html;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class PerformanceTest {

    private final HtmlParser parser = new HtmlParser();

    /**
     * Parses a large synthetic HTML document within a time budget.
     * Tunables via system properties:
     * -Dparser.perf.sections=120
     * -Dparser.perf.items=40
     * -Dparser.perf.timeoutSeconds=3
     */
    @Test
    void parseLargeDocumentWithinTimeBudget() {
        int sections = Integer.getInteger("parser.perf.sections", 120);
        int itemsPerSection = Integer.getInteger("parser.perf.items", 40);
        int timeoutSeconds = Integer.getInteger("parser.perf.timeoutSeconds", 1);

        String html = generateLargeHtml(sections, itemsPerSection);

        // Use preemptive timeout to avoid flakiness while still guarding performance.
        assertTimeoutPreemptively(Duration.ofSeconds(timeoutSeconds), () -> {
            for (var i = 0; i < 10; i++) {
                long t0 = System.currentTimeMillis();
                var document = parser.parse(html);
                long t1 = System.currentTimeMillis();
                System.out.printf("Parsed %,d chars in %d ms (%,.1f chars/sec)%n",
                        html.length(), (t1 - t0), html.length() * 1000.0 / (t1 - t0));
                // sanity checks (avoid asserting exact serialization for perf test)
                assertThat(document).isNotNull();
                assertThat(document.getDocumentElement().getLocalName()).isEqualTo("html");
                assertThat(document.asString()).isNotEmpty();
            }
        });
    }

    /**
     * Build a big but deterministic HTML that exercises tokenizer, parts, and tree builder.
     */
    private String generateLargeHtml(int sections, int itemsPerSection) {
        StringBuilder sb = new StringBuilder(sections * itemsPerSection * 160 + 2048);
        sb.append("<html><head><title>Perf</title></head><body>");

        for (int i = 0; i < sections; i++) {
            sb.append("\n<!-- section ").append(i).append(" -->\n");
            sb.append("<div class=\"section\" data-index=\"").append(i).append("\">");

            for (int j = 0; j < itemsPerSection; j++) {
                sb.append("\n  <a page=\"home\">\n")
                        .append("    Item ").append(i).append('-').append(j).append('\n')
                        // custom XML-style tag (must be NO_CONTENT)
                        .append("    <xis:param name=\"p").append(j).append("\" value=\"v").append(j).append("\"/>\n")
                        // boolean attribute (HTML) + void element cases
                        .append("    <input type=\"checkbox\" checked>\n")
                        .append("    <img src=\"img").append(j).append(".png\" alt=\"\"/>\n")
                        .append("  </a>\n");

                // sprinkle some normal flow content and optional end-tag parent
                if (j % 5 == 0) {
                    sb.append("  <p>Paragraph ").append(i).append('-').append(j).append("</p>\n");
                }
            }

            sb.append("</div>\n");
        }

        sb.append("</body></html>");
        return sb.toString();
    }
}