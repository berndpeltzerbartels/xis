package one.xis.jscomponent;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import one.xis.resource.ResourceFile;

@Getter
class Page implements ResourceFile, JavascriptComponent {

    private final Object pageController;
    private final ResourceFile htmlResourceFile;

    @Setter
    private boolean compiled;

    @Setter
    private String javascript;

    Page(@NonNull Object pageController, @NonNull ResourceFile htmlResourceFile) {
        this.pageController = pageController;
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
