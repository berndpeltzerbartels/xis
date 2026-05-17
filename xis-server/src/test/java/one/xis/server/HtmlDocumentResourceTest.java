package one.xis.server;

import one.xis.html.HtmlParser;
import one.xis.resource.Resource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlDocumentResourceTest {

    @Test
    void reparsesHtmlWhenSourceResourceBecomesObsolete() {
        MutableResource source = new MutableResource("<html><head></head><body><p>first</p></body></html>");
        HtmlDocumentResource resource = new HtmlDocumentResource(source, new HtmlParser(), getClass(), "Page", List.of());

        source.changeTo("<html><head></head><body><p>second</p></body></html>");

        assertTrue(resource.getContent().contains("second"));
    }

    @Test
    void appliesHtmlDocumentTransformersAfterParsingAndReparsing() {
        MutableResource source = new MutableResource("<html><head></head><body><p>first</p></body></html>");
        var transformations = new AtomicInteger();
        HtmlDocumentTransformer transformer = document -> {
            transformations.incrementAndGet();
            document.getDocumentElement().getElementByTagName("body").setAttribute("data-transformed", "true");
            return document;
        };
        HtmlDocumentResource resource = new HtmlDocumentResource(source, new HtmlParser(), getClass(), "Page", List.of(transformer));

        assertTrue(resource.getContent().contains("data-transformed=\"true\""));

        source.changeTo("<html><head></head><body><p>second</p></body></html>");

        assertTrue(resource.getContent().contains("second"));
        assertTrue(resource.getContent().contains("data-transformed=\"true\""));
        assertEquals(2, transformations.get());
    }

    private static class MutableResource implements Resource {
        private final String path = "test.html";
        private String content;
        private long lastModified = 1;
        private boolean obsolete;

        MutableResource(String content) {
            this.content = content;
        }

        void changeTo(String content) {
            this.content = content;
            lastModified++;
            obsolete = true;
        }

        @Override
        public int getLength() {
            return content.length();
        }

        @Override
        public String getContent() {
            obsolete = false;
            return content;
        }

        @Override
        public long getLastModified() {
            return lastModified;
        }

        @Override
        public String getResourcePath() {
            return path;
        }

        @Override
        public boolean isObsolete() {
            return obsolete;
        }
    }
}
