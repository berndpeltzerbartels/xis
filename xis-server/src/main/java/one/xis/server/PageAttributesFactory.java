package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.WelcomePage;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
class PageAttributesFactory {

    private final PathResolver pathResolver;
    
    PageAttributes attributes(Object controller) {
        var attributes = new PageAttributes();
        var path = pathResolver.createPath(PageUtil.getUrl(controller));
        attributes.setWelcomePage(controller.getClass().isAnnotationPresent(WelcomePage.class));
        attributes.setPath(path);
        attributes.setNormalizedPath(path.normalized());
        return attributes;
    }

}
