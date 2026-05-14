package one.xis.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenericResource<T> implements Resource {
    @Getter
    private final T objectContent;
    @Getter
    private final long lastModified;
    @Getter
    private final String resourcePath;

    @Override
    public int getLength() {
        return objectContent != null ? objectContent.toString().length() : 0;
    }

    @Override
    public String getContent() {
        return objectContent != null ? objectContent.toString() : "";
    }

    @Override
    public boolean isObsolete() {
        return false;
    }
}

