package one.xis.resource;

import lombok.Getter;

class EmptyResource implements Resource {

    @Getter
    private final long lastModified;

    EmptyResource() {
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
