package one.xis.page;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.jsc.JavascriptComponentUtils;
import one.xis.jsc.JavascriptComponents;
import one.xis.resource.ResourceFile;

import java.util.HashSet;
import java.util.Set;

import static one.xis.path.PathUtils.stripSuffix;
import static one.xis.path.PathUtils.stripTrailingSlash;

@XISComponent
@RequiredArgsConstructor
public class PageJavascripts extends JavascriptComponents<PageJavascript> {

    private final PageFactory pageFactory;
    private final PageJavascriptCompiler pageJavascriptCompiler;

    private final Set<String> pathsForValidation = new HashSet<>();

    @Getter
    private PageJavascript welcomePage;

    @Override
    protected PageJavascript createComponent(Object controller) {
        return pageFactory.createPage(controller);
    }

    @Override
    protected String compile(String key, ResourceFile resourceFile, String javascriptClassName) {
        return pageJavascriptCompiler.compile(key, resourceFile, javascriptClassName);
    }

    @Override
    public PageJavascript add(Object controller) {
        PageJavascript pageJavascript = super.add(controller);
        if (isWelcomePage(controller)) {
            if (welcomePage != null) {
                throw new IllegalStateException("more then one welcome-page defined (@Page(welcomePage=true))");
            }
            welcomePage = pageJavascript;
        }
        return pageJavascript;
    }

    @Override
    protected String createKey(Object pageController) {
        String path = getPath(pageController);
        JavascriptComponentUtils.validatePath(path);
        String normalizedPath = normalizePath(path);
        validateNotDuplicate(normalizedPath);
        return JavascriptComponentUtils.pathToUrn(normalizedPath);
    }

    private String normalizePath(String path) {
        String normalizedPath = stripSuffix(path);
        return stripTrailingSlash(normalizedPath);
    }

    private String getPath(Object pageController) {
        return pageController.getClass().getAnnotation(one.xis.Page.class).path();
    }

    private boolean isWelcomePage(Object pageController) {
        return pageController.getClass().getAnnotation(one.xis.Page.class).welcomePage();
    }

    private void validateNotDuplicate(String normalizedPath) {
        if (pathsForValidation.contains(normalizedPath)) {
            throw new IllegalStateException("there is more tham one page with path " + normalizedPath);
        }
        pathsForValidation.add(normalizedPath);
    }

}
