/**
 * @class FrontletButtonHandler
 * @extends {FrontletLinkHandlerBase}
 * @package classes/handler
 * @access public
 * @description Handler for <button> elements linking to frontlets.
 */
class FrontletButtonHandler extends FrontletLinkHandlerBase {

    /**
     * 
     * @param {Element} element 
     * @param {FrontletContainers} frontletContainers
     */
    constructor(element, frontletContainers) {
        super(element, frontletContainers);
        this.type = 'frontlet-button-handler';
    }

}
