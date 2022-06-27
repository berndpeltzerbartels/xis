package one.xis.widget;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import one.xis.resource.ReloadableResourceFile;
import one.xis.resource.ResourceFile;

@Getter
public class Widget implements ResourceFile {

    private final String widgetClass;
    private final ResourceFile htmlResourceFile;

    @Setter(AccessLevel.PACKAGE)
    private String javascript;

    Widget(@NonNull String widgetClass, @NonNull ResourceFile htmlResourceFile) {
        this.widgetClass = widgetClass;
        this.htmlResourceFile = htmlResourceFile;
    }

    String getHtmlSrc() {
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

    public boolean isObsolete() {
        if (htmlResourceFile instanceof ReloadableResourceFile) {
            ReloadableResourceFile reloadableResourceFile = (ReloadableResourceFile) htmlResourceFile;
            return reloadableResourceFile.isObsolete();
        }
        return false;
    }

    public void reloadHtml() {
        if (htmlResourceFile instanceof ReloadableResourceFile) {
            ReloadableResourceFile reloadableResourceFile = (ReloadableResourceFile) htmlResourceFile;
            reloadableResourceFile.reload();
        } else {
            throw new IllegalStateException();
        }
    }
}
