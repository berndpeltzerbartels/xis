/**
 * @class FrontletLinkHandler
 * @extends {FrontletLinkHandlerBase}
 * @package classes/handler
 * @access public
 * @description Handler for <a> elements linking to widgets.
 */
class FrontletLinkHandler extends FrontletLinkHandlerBase {

    /**
     * 
     * @param {Element} element 
     * @param {FrontletContainers} widgetContainers
     */
    constructor(element, widgetContainers) {
        super(element, widgetContainers);
        this.type = 'widget-link-handler';
        element.setAttribute("href", "#");
    }

}

