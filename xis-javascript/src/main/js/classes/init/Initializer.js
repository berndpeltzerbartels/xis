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
     * @param {Includes} includes
     * @param {WidgetContainers} widgetContainers
     * @param {TagHandlers} tagHandlers
     * @param {TagRegistry} tagRegistry
     */
    constructor(domAccessor, client, widgets, includes, widgetContainers, tagHandlers, tagRegistry) {
        this.handlerBuilder = new HandlerBuilder(domAccessor, client, widgets, includes, widgetContainers, tagHandlers, this, tagRegistry);
        this.domAccessor = domAccessor;
        this.tagRegistry = tagRegistry;
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
        if (!isElement(element) && !isDocumentFragment(element)) {
            return element;
        }
        return new DomNormalizer(element, this.domAccessor, this.tagRegistry).normalize();
    }
}
