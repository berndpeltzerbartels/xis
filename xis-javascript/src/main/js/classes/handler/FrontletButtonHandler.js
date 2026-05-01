/**
 * @class FrontletButtonHandler
 * @extends {FrontletLinkHandlerBase}
 * @package classes/handler
 * @access public
 * @description Handler for <button> elements linking to widgets.
 */
class FrontletButtonHandler extends FrontletLinkHandlerBase {

    /**
     * 
     * @param {Element} element 
     * @param {FrontletContainers} widgetContainers
     */
    constructor(element, widgetContainers) {
        super(element, widgetContainers);
        this.type = 'widget-button-handler';
    }

}
