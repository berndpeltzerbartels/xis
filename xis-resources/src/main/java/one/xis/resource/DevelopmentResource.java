package one.xis.resource;


import lombok.extern.slf4j.Slf4j;
import one.xis.utils.io.IOUtils;

import java.io.File;

/**
 * Represents a classpath-resource in build-folder not inside a jar.
 * This is intended to allow changes to the resource to have effect without
 * restart.
 */
@Slf4j
class DevelopmentResource implements ReloadableResource {

    private final String path;
    private final File file;
    private String content;
    private long lastModified;

    DevelopmentResource(String path, File file) {
        this.path = path;
        this.file = file;
        reload();
    }

    @Override
    public int getLength() {
        return getContent().length();
    }


    @Override
    public String getContent() {
        if (isObsolete()) {
            reload();
        }
        return content;
    }

    @Override
    public String getResourcePath() {
        return path;
    }
    
    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public boolean isObsolete() {
        synchronized (this) {
            return file.lastModified() > lastModified;
        }
    }

    @Override
    public void reload() {
        synchronized (this) {
            log.info("loading {}", file.getAbsolutePath());
            content = IOUtils.getContent(file, "utf-8");
            lastModified = file.lastModified();
        }
    }
}
