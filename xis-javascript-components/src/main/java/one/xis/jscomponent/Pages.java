package one.xis.jscomponent;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ResourceFile;

import java.util.HashSet;
import java.util.Set;

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
    protected String compile(String name, ResourceFile resourceFile) {
        return pageCompiler.compile(name, resourceFile);
    }

    @Override
    protected String createKey(String name, Object pageController) {
        String path = getPath(pageController);
        validatePath(path, pageController);
        return path;
    }

    private String getPath(Object pageController) {
        return pageController.getClass().getAnnotation(one.xis.Page.class).path();
    }

    private void validatePath(@NonNull String path, @NonNull Object pageController) {
        if (pathsForValidation.contains(path)) {
            throw new DuplicateKeyException(String.format("there is more than one page with path '%s'", path));
        }
        pathsForValidation.add(path);
        if (!path.endsWith(".html")) {
            throw new IllegalStateException("path in " + pageController.getClass() + " must have suffix 'html'");
        }
    }

}
