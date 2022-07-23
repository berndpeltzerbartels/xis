package one.xis.page;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ResourceFile;

@XISComponent
@RequiredArgsConstructor
public class PageService {

    private final PageJavascripts pageJavascripts;
    private final PageControllers pageControllers;
    private final RootPage rootPage;

    // TODO remove all "public" ?
    public void addPageController(Object controller) {
        pageJavascripts.add(controller);
        pageControllers.add(controller);
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
}
