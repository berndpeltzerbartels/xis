package one.xis.resource;

import lombok.Getter;
import one.xis.utils.io.IOUtils;

public class ClassPathResource implements Resource {

    @Getter
    private final String content;
    private final String path;

    public ClassPathResource(String path) {
        this.path = path;
        content = IOUtils.getResourceAsString(path);
    }

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
        return path;
    }
}
