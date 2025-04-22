package one.xis.server;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.WelcomePage;
import one.xis.context.XISComponent;
import one.xis.resource.Resources;

import java.util.Optional;

@XISComponent
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
        addParameterAttributes(controller.getClass(), attributes);
        getJavascriptResource(controller).ifPresent(attributes::setPageJavascriptSource);
        return attributes;
    }

    private Optional<String> getJavascriptResource(@NonNull Object controller) {
        var javascriptPath = PageUtil.getJavascriptResourcePath(controller.getClass());
        return resources.exists(javascriptPath) ? Optional.of("/xis/page/javascript/" + javascriptPath) : Optional.empty();
    }

}
