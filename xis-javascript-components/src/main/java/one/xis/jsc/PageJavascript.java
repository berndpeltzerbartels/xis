package one.xis.jsc;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import one.xis.resource.ResourceFile;

@Getter
class PageJavascript implements ResourceFile, JavascriptComponent {

    private final ResourceFile htmlResourceFile;
    private final String javascriptClass;

    @Setter
    private boolean compiled;

    @Setter
    private String javascript;

    PageJavascript(@NonNull ResourceFile htmlResourceFile, String javascriptClass) {
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
