/**
 * @class FrontletLinkHandler
 * @extends {FrontletLinkHandlerBase}
 * @package classes/handler
 * @access public
 * @description Handler for <a> elements linking to frontlets.
 */
class FrontletLinkHandler extends FrontletLinkHandlerBase {

    /**
     * 
     * @param {Element} element 
     * @param {FrontletContainers} frontletContainers
     */
    constructor(element, frontletContainers) {
        super(element, frontletContainers);
        this.type = 'frontlet-link-handler';
        element.setAttribute("href", "#");
    }

}

