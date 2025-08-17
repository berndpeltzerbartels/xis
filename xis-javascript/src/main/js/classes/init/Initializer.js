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
     * @param {TagHandlers} tagHandlers
     */
    constructor(domAccessor, client, widgets, widgetContainers, tagHandlers) {
        this.handlerBuilder = new HandlerBuilder(domAccessor, client, widgets, widgetContainers, tagHandlers);
        this.domAccessor = domAccessor;
    }


    /**
     * 
     * @param {Node} node 
     * @param {TagHandler} parentHandler 
     */
    initialize(node, parentHandler) {
        return this.handlerBuilder.create(node, parentHandler);
    }

    /**
     * 
     * @param {Element} element 
     * @returns 
     */
    normalizeElement(element) {
        if (!isElement(element)) {
            return element;
        }
        return new DomNormalizer(element, this.domAccessor).normalize();
    }
}
