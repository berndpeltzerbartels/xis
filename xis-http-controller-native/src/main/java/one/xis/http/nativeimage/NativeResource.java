package one.xis.http.nativeimage;

import one.xis.resource.Resource;

final class NativeResource implements Resource {

    private final String path;
    private final String content;
    private final long lastModified;

    NativeResource(String path, String content) {
        this.path = path;
        this.content = content;
        this.lastModified = System.currentTimeMillis();
    }

    @Override
    public int getLength() {
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
    public String getResourcePath() {
        return path;
    }

    @Override
    public boolean isObsolete() {
        return false;
    }
}
