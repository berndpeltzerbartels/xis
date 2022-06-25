package one.xis.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InMemoryResource implements Resource {

    private final String content;
    private final String contentType;
    private final long lastModified;

    @Override
    public int getLenght() {
        return content.length();
    }
}
