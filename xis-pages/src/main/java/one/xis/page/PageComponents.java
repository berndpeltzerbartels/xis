package one.xis.page;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@XISComponent
@RequiredArgsConstructor
class PageComponents {

    private final Map<String, PageComponent> pageComponentsByClassname = new HashMap<>();
    private final Map<String, PageComponent> pageComponentsByPath = new HashMap<>();
    private final PageComponentCompiler pageComponentCompiler;
    private PageComponent welcomePageComponent;

    PageComponent createPageComponent(PageMetaData pageMetaData) {
        PageComponent pageComponent = createComponent(pageMetaData);
        if (pageMetaData.isWelcomePage()) {
            if (welcomePageComponent != null) {
                throw new IllegalStateException("more then one welcome-page defined (@Page(welcomePage=true))");
            }
            welcomePageComponent = pageComponent;
        }
        pageComponentsByClassname.put(pageMetaData.getControllerClass().getName(), pageComponent);
        pageComponentsByPath.put(pageMetaData.getPath(), pageComponent);
        return pageComponent;
    }

    public PageComponent getByComponentClass(String jsClassname) {
        PageComponent pageComponent = pageComponentsByClassname.get(jsClassname);
        synchronized (pageComponent) {
            pageComponentCompiler.compileIfObsolete(pageComponent);
        }
        return pageComponent;
    }

    private PageComponent createComponent(PageMetaData pageMetaData) {
        var pageComponent = PageComponent.builder()
                .htmlResource(pageMetaData.getHtmlTemplate())
                .controllerClass(pageMetaData.getControllerClass())
                .path(pageMetaData.getPath())
                .javascriptClass(pageMetaData.getJavascriptClassname())
                .build();
        pageComponentCompiler.compile(pageComponent);
        return pageComponent;
    }

    Collection<String> getClassnames() {
        return pageComponentsByClassname.keySet();
    }

}
