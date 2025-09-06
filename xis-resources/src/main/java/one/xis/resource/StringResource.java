package one.xis.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StringResource implements Resource {
    @Getter
    private final String content;
    private final int lastModified;


    public StringResource(String content) {
        this.content = content;
        this.lastModified = (int) System.currentTimeMillis();
    }

    @Override
    public int getLength() {
        return content.length();
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public String getResourcePath() {
        return "";
    }

    @Override
    public boolean isObsolete() {
        return false;
    }
}
