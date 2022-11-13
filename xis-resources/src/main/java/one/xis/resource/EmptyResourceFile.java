package one.xis.resource;

import lombok.Getter;

class EmptyResourceFile implements ResourceFile {

    @Getter
    private final long lastModified;

    EmptyResourceFile() {
        lastModified = System.currentTimeMillis();
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public String getContent() {
        return "";
    }

}
