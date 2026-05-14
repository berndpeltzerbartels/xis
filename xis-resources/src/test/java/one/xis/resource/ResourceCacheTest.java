package one.xis.resource;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceCacheTest {

    @Test
    void reloadsContentWhenResourceBecomesObsolete() {
        MutableResource resource = new MutableResource("first");
        AtomicInteger loads = new AtomicInteger();
        ResourceCache<String> cache = new ResourceCache<>(
                current -> loads.incrementAndGet() + ":" + current.getContent(),
                Map.of("item", resource));

        assertEquals(Optional.of("1:first"), cache.getResourceContent("item"));

        resource.changeTo("second");

        assertEquals(Optional.of("2:second"), cache.getResourceContent("item"));
    }

    private static class MutableResource implements Resource {
        private String content;
        private boolean obsolete;

        MutableResource(String content) {
            this.content = content;
        }

        void changeTo(String content) {
            this.content = content;
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
            return 0;
        }

        @Override
        public String getResourcePath() {
            return "test";
        }

        @Override
        public boolean isObsolete() {
            return obsolete;
        }
    }
}
