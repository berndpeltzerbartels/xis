package one.xis.page;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
class PageComponents {

    private final Map<String, PageComponent> pageJavascriptsByClassname = new HashMap<>();
    private final Map<String, PageComponent> pageJavascriptsByPath = new HashMap<>();

    private final PageComponentCompiler pageComponentCompiler;

    @Getter
    private PageComponent welcomePageComponent;

    PageComponent createScript(PageMetaData pageMetaData) {
        PageComponent pageComponent = createPageJavascript(pageMetaData);
        if (pageMetaData.isWelcomePage()) {
            if (welcomePageComponent != null) {
                throw new IllegalStateException("more then one welcome-page defined (@Page(welcomePage=true))");
            }
            welcomePageComponent = pageComponent;
        }
        pageJavascriptsByClassname.put(pageMetaData.getControllerClass().getName(), pageComponent);
        pageJavascriptsByPath.put(pageMetaData.getPath(), pageComponent);
        return pageComponent;
    }

    public PageComponent getByComponentClass(String jsClassname) {
        PageComponent pageComponent = pageJavascriptsByClassname.get(jsClassname);
        synchronized (pageComponent) {
            pageComponentCompiler.compileIfObsolete(pageComponent);
        }
        return pageComponent;
    }

    private PageComponent createPageJavascript(PageMetaData pageMetaData) {
        var pageComponent = PageComponent.builder()
                .htmlResourceFile(pageMetaData.getHtmlTemplate())
                .controllerClass(pageMetaData.getControllerClass())
                .path(pageMetaData.getPath())
                .javascriptClass(pageMetaData.getJavascriptClassname())
                .build();
        pageComponentCompiler.compile(pageComponent);
        return pageComponent;
    }


    Collection<String> getClassnames() {
        return pageJavascriptsByClassname.keySet();
    }

    Map<String, PageComponent> getPagesByPath() {
        return pageJavascriptsByPath;
    }
}
