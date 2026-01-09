/**
 * @class WidgetButtonHandler
 * @extends {WidgetLinkHandlerBase}
 * @package classes/handler
 * @access public
 * @description Handler for <button> elements linking to widgets.
 */
class WidgetButtonHandler extends WidgetLinkHandlerBase {

    /**
     * 
     * @param {Element} element 
     * @param {WidgetContainers} widgetContainers
     */
    constructor(element, widgetContainers) {
        super(element, widgetContainers);
        this.type = 'widget-button-handler';
    }

}
