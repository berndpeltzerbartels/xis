package one.xis.server;


import one.xis.context.XISComponent;

@XISComponent
class WidgetAttributesFactory extends ComponentAttributesFactory<ComponentAttributes> {

    @Override
    ComponentAttributes attributes(Object controller) {
        var attributes = new ComponentAttributes();
        attributes.setModelParameterNames(modelsToSubmitForModel(controller));
        attributes.setActionParameterNames(modelsToSubmitForAction(controller));
        return attributes;
    }
}
