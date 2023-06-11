package one.xis.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StringResource implements Resource {
    @Getter
    private final String content;

    @Override
    public int getLength() {
        return content.length();
    }

    @Override
    public long getLastModified() {
        return 0;
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
