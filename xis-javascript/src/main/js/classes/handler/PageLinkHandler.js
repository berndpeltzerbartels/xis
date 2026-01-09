/**
 * @class PageLinkHandler
 * @extends {PageLinkHandlerBase}
 * @package classes/handler
 * @access public
 * @description Handler for <a> elements linking to pages.
 */
class PageLinkHandler extends PageLinkHandlerBase {

    /**
     * 
     * @param {Element} element 
     */
    constructor(element) {
        super(element);
        this.type = 'page-link-handler';
        element.setAttribute("href", "#");
    }

}


