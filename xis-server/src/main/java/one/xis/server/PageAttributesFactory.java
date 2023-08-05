package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.Page;
import one.xis.WelcomePage;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
class PageAttributesFactory extends ComponentAttributesFactory<PageAttributes> {

    private final PathResolver pathResolver;

    @Override
    PageAttributes attributes(Object controller) {
        var attributes = new PageAttributes();
        var path = pathResolver.create(controller.getClass().getAnnotation(Page.class).value());
        attributes.setModelsToSubmitOnRefresh(modelsToSubmitForModel(controller));
        attributes.setModelsToSubmitOnAction(modelsToSubmitForAction(controller));
        attributes.setWelcomePage(controller.getClass().isAnnotationPresent(WelcomePage.class));
        attributes.setPath(path);
        attributes.setNormalizedPath(path.normalized());
        return attributes;
    }

}
