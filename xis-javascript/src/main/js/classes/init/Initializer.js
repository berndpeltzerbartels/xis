class Initializer {

    /**
     *
     * @param {DomAccessor} domAccessor
     * @param {Client} client
     * @param {Widgets} widgets
     * @param {WidgetContainers} widgetContainers
     */
    constructor(domAccessor, client, widgets, widgetContainers) {
        this.tagHandlerDecorator = new NodeDecorator(domAccessor, client, widgets, widgetContainers);
        this.domAccessor = domAccessor;
    }


    initialize(node, parentHandler) {
        if (isElement(node)) {
            new DomNormalizer(node, this.domAccessor).normalize();
        }
        this.tagHandlerDecorator.decorate(node, parentHandler);
    }
}
