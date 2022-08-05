package one.xis.page;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.js.JavascriptComponent;
import one.xis.resource.ResourceFile;

@Getter
@Setter
@RequiredArgsConstructor
public class PageJavascript implements ResourceFile, JavascriptComponent {
    private final ResourceFile htmlResourceFile;
    private final String javascriptClass;
    private final String controllerClassName;
    private String key;
    private boolean compiled;
    private String javascript;

    @Override
    public int getLenght() {
        return javascript.length();
    }

    @Override
    public String getContent() {
        return javascript;
    }

    @Override
    public long getLastModified() {
        return htmlResourceFile.getLastModified();
    }

}
