package one.xis.resource;


import one.xis.utils.io.IOUtils;

import java.io.File;

/**
 * Represents a classpath-resource in build-folder not inside a jar.
 * This is intended to allow changes to the resource to have effect without
 * restart.
 */
class DevelopmentResource implements ReloadableResourceFile {

    private final File file;
    private String content;
    private long lastModified;

    DevelopmentResource(File file) {
        this.file = file;
        reload();
    }

    @Override
    public int getLenght() {
        return content.length();
    }


    @Override
    public String getContent() {
        return content;
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public boolean isObsolete() {
        return file.lastModified() > lastModified;
    }

    @Override
    public void reload() {
        content = IOUtils.getContent(file, "utf-8");
        lastModified = file.lastModified();
    }
}
