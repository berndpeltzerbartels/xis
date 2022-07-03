package one.xis.jscomponent;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ResourceFile;

@XISComponent
@RequiredArgsConstructor
class Pages extends JavascriptComponents<Page> {

    private final PageFactory pageFactory;
    private final PageCompiler pageCompiler;

    @Override
    protected Page createComponent(Object controller) {
        return pageFactory.createPage(controller);
    }

    @Override
    protected String compile(String name, ResourceFile resourceFile) {
        return pageCompiler.compile(name, resourceFile);
    }
}
