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

    private final Map<String, PageComponent> pageComponentsByComponentClassname = new HashMap<>();
    private final Map<Class<?>, PageComponent> pageComponentsByControllerClass = new HashMap<>();

    @Getter
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
        pageComponentsByComponentClassname.put(pageMetaData.getJavascriptClassname(), pageComponent);
        pageComponentsByControllerClass.put(pageMetaData.getControllerClass(), pageComponent);
        pageComponentsByPath.put(pageComponent.getPath(), pageComponent);
        return pageComponent;
    }

    PageComponent getByComponentClass(String jsClassname) {
        PageComponent pageComponent = pageComponentsByComponentClassname.get(jsClassname);
        synchronized (pageComponent) {
            pageComponentCompiler.compileIfObsolete(pageComponent);
        }
        return pageComponent;
    }

    PageComponent getByControllerClass(Class<?> controllerClass) {
        PageComponent pageComponent = pageComponentsByControllerClass.get(controllerClass);
        synchronized (pageComponent) {
            pageComponentCompiler.compileIfObsolete(pageComponent);
        }
        return pageComponent;
    }

    PageComponent getByPath(String path) {
        if (path.endsWith(".html")) {
            throw new IllegalArgumentException(path + " must have suffix .html");
        }
        PageComponent pageComponent = pageComponentsByPath.get(path);
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
        return pageComponentsByComponentClassname.keySet();
    }


}
