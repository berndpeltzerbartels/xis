package one.xis.page;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import one.xis.js.JavascriptComponent;
import one.xis.resource.Resource;

@Getter
@Setter
@Builder
public class PageComponent implements Resource, JavascriptComponent {
    private final Resource htmlResource;
    private final String javascriptClass;
    private final String path;
    private final Class<?> controllerClass;
    private boolean compiled;
    private String javascript;

    @Override
    public int getLength() {
        return javascript.length();
    }

    @Override
    public String getContent() {
        return javascript;
    }

    @Override
    public long getLastModified() {
        return htmlResource.getLastModified();
    }
}
