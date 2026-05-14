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
     * @param {Client} client for action and model
     * @param {Frontlets} frontlets
     * @param {Includes} includes
     * @param {FrontletContainers} frontletContainers
     * @param {TagHandlers} tagHandlers
     */
    constructor(domAccessor, client, frontlets, includes, frontletContainers, tagHandlers) {
        this.handlerBuilder = new HandlerBuilder(domAccessor, client, frontlets, includes, frontletContainers, tagHandlers, this);
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
        if (!isElement(element) && !isDocumentFragment(element)) {
            return element;
        }
        return new DomNormalizer(element, this.domAccessor).normalize();
    }
}
