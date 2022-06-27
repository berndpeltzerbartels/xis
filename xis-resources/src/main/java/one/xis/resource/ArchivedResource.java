package one.xis.resource;

import lombok.Getter;
import one.xis.utils.io.IOUtils;

/**
 * For Resource inside a jar. Will not be reloaded.
 */
@Getter
class ArchivedResource implements ResourceFile {

    private final String content;
    private final long lastModified;

    ArchivedResource(String resource) {
        content = IOUtils.getResourceAsString(resource);
        lastModified = System.currentTimeMillis();
    }

    @Override
    public int getLenght() {
        return content.length();
    }
}
