package one.xis.jsc;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ResourceFile;

import java.util.HashSet;
import java.util.Set;

import static one.xis.path.PathUtils.stripSuffix;
import static one.xis.path.PathUtils.stripTrailingSlash;

@XISComponent
@RequiredArgsConstructor
class Pages extends JavascriptComponents<Page> {

    private final PageFactory pageFactory;
    private final PageCompiler pageCompiler;

    private final Set<String> pathsForValidation = new HashSet<>();

    @Override
    protected Page createComponent(Object controller) {
        return pageFactory.createPage(controller);
    }

    @Override
    protected String compile(String key, ResourceFile resourceFile, String javascriptClassName) {
        return pageCompiler.compile(key, resourceFile, javascriptClassName);
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

    private void validateNotDuplicate(String normalizedPath) {
        if (pathsForValidation.contains(normalizedPath)) {
            throw new IllegalStateException("there is more tham one page with path " + normalizedPath);
        }
        pathsForValidation.add(normalizedPath);
    }

}
