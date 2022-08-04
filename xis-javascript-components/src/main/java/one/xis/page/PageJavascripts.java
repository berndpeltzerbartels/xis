package one.xis.page;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.jsc.JavascriptComponents;
import one.xis.resource.ResourceFile;

import java.util.HashSet;
import java.util.Set;

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
    public PageJavascript add(String key, Object controller) {
        PageJavascript pageJavascript = super.add(key, controller);
        if (isWelcomePage(controller)) {
            if (welcomePage != null) {
                throw new IllegalStateException("more then one welcome-page defined (@Page(welcomePage=true))");
            }
            welcomePage = pageJavascript;
        }
        return pageJavascript;
    }


    private boolean isWelcomePage(Object pageController) {
        return pageController.getClass().getAnnotation(one.xis.Page.class).welcomePage();
    }
}
