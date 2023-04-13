package one.xis.resource;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import one.xis.utils.io.IOUtils;

/**
 * For Resource inside a jar. Will not be reloaded.
 */
@Slf4j
@Getter
class StaticResource implements Resource {

    private final String resource;
    private final String content;
    private final long lastModified;

    StaticResource(String resource) {
        log.info("loading {}", resource);
        this.resource = resource;
        content = IOUtils.getResourceAsString(resource);
        lastModified = System.currentTimeMillis();
    }

    @Override
    public String getResourcePath() {
        return resource;
    }


    @Override
    public int getLength() {
        return content.length();
    }
}
