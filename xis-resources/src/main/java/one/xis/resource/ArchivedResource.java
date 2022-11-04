package one.xis.resource;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import one.xis.utils.io.IOUtils;

/**
 * For Resource inside a jar. Will not be reloaded.
 */
@Slf4j
@Getter
class ArchivedResource implements ResourceFile {

    private final String content;
    private final long lastModified;

    ArchivedResource(String resource) {
        log.info("loading {}", resource);
        content = IOUtils.getResourceAsString(resource);
        lastModified = System.currentTimeMillis();
    }

    @Override
    public int getLenght() {
        return content.length();
    }
}
