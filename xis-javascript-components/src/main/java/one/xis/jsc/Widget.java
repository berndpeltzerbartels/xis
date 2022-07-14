package one.xis.jsc;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import one.xis.resource.ResourceFile;

@Getter
class Widget implements ResourceFile, JavascriptComponent {

    private final Object widgetController;
    private final ResourceFile htmlResourceFile;
    private final String javascriptClass;

    @Setter
    private boolean compiled;

    @Setter
    private String javascript;

    Widget(@NonNull Object widgetController, @NonNull ResourceFile htmlResourceFile, String javascriptClass) {
        this.widgetController = widgetController;
        this.htmlResourceFile = htmlResourceFile;
        this.javascriptClass = javascriptClass;
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
