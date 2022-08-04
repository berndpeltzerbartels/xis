package one.xis.page;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.jsc.JavascriptComponentUtils;
import one.xis.resource.ResourceFile;

import static one.xis.path.PathUtils.stripSuffix;
import static one.xis.path.PathUtils.stripTrailingSlash;

@XISComponent
@RequiredArgsConstructor
public class PageService {

    private final PageJavascripts pageJavascripts;
    private final PageControllers pageControllers;
    private final RootPage rootPage;

    // TODO remove all "public" ?
    public void addPageController(Object controller) {
        String key = createKey(controller);
        pageJavascripts.add(key, controller);
        pageControllers.add(key, controller);
    }

    public String getRootPageHtml() {
        return rootPage.getContent();
    }

    public ResourceFile getPage(String key) {
        return pageJavascripts.get(key);
    }

    public void createRootContent() {
        rootPage.createContent();
    }


    protected String createKey(Object pageController) {
        String path = getPath(pageController);
        JavascriptComponentUtils.validatePath(path);
        String normalizedPath = normalizePath(path);
        return JavascriptComponentUtils.pathToUrn(normalizedPath);
    }

    private String normalizePath(String path) {
        String normalizedPath = stripSuffix(path);
        return stripTrailingSlash(normalizedPath);
    }

    private String getPath(Object pageController) {
        return pageController.getClass().getAnnotation(one.xis.Page.class).path();
    }

}
