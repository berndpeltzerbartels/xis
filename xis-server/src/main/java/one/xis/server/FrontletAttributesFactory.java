package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.context.Component;

@Component
@RequiredArgsConstructor
class FrontletAttributesFactory extends AttributesFactory {

    private final ComponentHostResolver hostResolver;

    FrontletAttributes attributes(Object controller) {
        var attributes = new FrontletAttributes();
        var frontletId = FrontletUtil.getId(controller);
        attributes.setId(frontletId);
        attributes.setUrl(FrontletUtil.getUrl(controller.getClass()));
        attributes.setHost(hostResolver.getFrontletHost(frontletId));
        addParameterAttributes(controller.getClass(), attributes);
        addUpdateEventKeys(controller.getClass(), attributes);
        return attributes;
    }
}
