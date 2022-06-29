package one.xis.widget;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import one.xis.resource.ResourceFile;

@Getter
public class Widget implements ResourceFile {

    private final Object widgetController;
    private final ResourceFile htmlResourceFile;

    @Setter(AccessLevel.PACKAGE)
    private String javascript;

    Widget(@NonNull Object widgetController, @NonNull ResourceFile htmlResourceFile) {
        this.widgetController = widgetController;
        this.htmlResourceFile = htmlResourceFile;
    }

    String getId() {
        return widgetController.getClass().getName();
    }

    String getHtmlTemplate() {
        return htmlResourceFile.getContent();
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
