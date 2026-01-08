package one.xis.server;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.WelcomePage;
import one.xis.context.Component;
import one.xis.resource.Resources;

@Component
@RequiredArgsConstructor
class PageAttributesFactory extends AttributesFactory {

    private final PathResolver pathResolver;
    private final Resources resources;

    PageAttributes attributes(@NonNull Object controller) {
        var attributes = new PageAttributes();
        var path = pathResolver.createPath(PageUtil.getUrl(controller));
        attributes.setWelcomePage(controller.getClass().isAnnotationPresent(WelcomePage.class));
        attributes.setPath(path);
        attributes.setNormalizedPath(path.normalized());
        if (attributes.isWelcomePage() && path.hasPathVariables()) {
            throw new IllegalStateException("WelcomePage cannot have path variables: " + controller.getClass());
        }
        addParameterAttributes(controller.getClass(), attributes);
        addUpdateEventKeys(controller.getClass(), attributes);
        return attributes;
    }


}
