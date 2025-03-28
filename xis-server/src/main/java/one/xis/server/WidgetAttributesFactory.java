package one.xis.server;


import one.xis.context.XISComponent;

@XISComponent
class WidgetAttributesFactory extends AttributesFactory {

    WidgetAttributes attributes(Object controller) {
        var attributes = new WidgetAttributes();
        attributes.setHost(null); // TODO
        attributes.setId(WidgetUtil.getId(controller));
        addParameterAttributes(controller.getClass(), attributes);
        return attributes;
    }
}
