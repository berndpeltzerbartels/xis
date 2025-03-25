/**
 * @class Initializer
 * @package classes/init
 * @access public
 * @description Prepares the dom for using the framework's tag handlers.
 */
class Initializer {

    /**
     *
     * @param {DomAccessor} domAccessor
     * @param {HttpClient} client
     * @param {Widgets} widgets
     * @param {WidgetContainers} widgetContainers
     */
    constructor(domAccessor, client, widgets, widgetContainers) {
        this.tagHandlerDecorator = new NodeDecorator(domAccessor, client, widgets, widgetContainers);
        this.domAccessor = domAccessor;
    }


    /**
     * 
     * @param {Node} node 
     * @param {TagHandler} parentHandler 
     */
    initialize(node, parentHandler) {
        if (isElement(node)) {
            new DomNormalizer(node, this.domAccessor).normalize();
        }
        this.tagHandlerDecorator.decorate(node, parentHandler);
    }
}
