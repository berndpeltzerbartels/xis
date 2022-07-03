package one.xis.jscomponent;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import one.xis.resource.ResourceFile;

@Getter
class Widget implements ResourceFile, JavascriptComponent {

    private final Object widgetController;
    private final ResourceFile htmlResourceFile;

    @Setter
    private boolean compiled;

    @Setter
    private String javascript;

    Widget(@NonNull Object widgetController, @NonNull ResourceFile htmlResourceFile) {
        this.widgetController = widgetController;
        this.htmlResourceFile = htmlResourceFile;
    }

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
