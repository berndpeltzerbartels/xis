package one.xis.page;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import one.xis.controller.ControllerModel;
import one.xis.js.JavascriptComponent;
import one.xis.resource.ResourceFile;

@Getter
@Setter
@Builder
public class PageJavascript implements ResourceFile, JavascriptComponent {
    private final ResourceFile htmlResourceFile;
    private final String javascriptClass;
    private final String path;
    private final ControllerModel controllerModel;
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
