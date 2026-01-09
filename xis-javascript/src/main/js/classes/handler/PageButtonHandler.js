/**
 * @class PageButtonHandler
 * @extends {PageLinkHandlerBase}
 * @package classes/handler
 * @access public
 * @description Handler for <button> elements linking to pages.
 */
class PageButtonHandler extends PageLinkHandlerBase {

    /**
     * 
     * @param {Element} element 
     */
    constructor(element) {
        super(element);
        this.type = 'page-button-handler';
    }

}
