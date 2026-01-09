/**
 * @class PageLinkHandlerBase
 * @extends {TagHandler}
 * @package classes/handler
 * @access public
 * @description Base class for handling links to pages.
 * 
 * @property {String} type
 * @property {Object} parameters
 * @property {String} targetPageUrl
 * @property {String} targetWidgetUrl
 * @property {String} targetContainerId
 */
class PageLinkHandlerBase extends TagHandler {

    /**
     * 
     * @param {Element} element 
     */
    constructor(element) {
        super(element);
        this.type = 'page-link-handler-base';
        this.parameters = {};
        this.targetPageUrl = undefined;
        this.targetWidgetUrl = undefined;
        this.targetContainerId = undefined;
        element.addEventListener('click', event => {
            event.preventDefault();
            this.onClick(event);
        });
    }

    /**
     * @public
     * @param {Data} data 
     */
    refresh(data) {
        this.data = data;
        this.parameters = {};
        const descendantPromise = this.refreshDescendantHandlers(data); // attributes might have variables !
        this.targetPageUrl = this.tag.getAttribute('xis:page');
        this.targetWidgetUrl = this.tag.getAttribute('xis:widget');
        this.targetContainerId = this.tag.getAttribute('xis:target-container');
        return descendantPromise;
    }
    
    /**
     * 
     * @param {String} name 
     * @param {String} value 
     */
    addParameter(name, value) {
        this.parameters[name] = value;
    }


    /**
     * @public
     * @param {Event} e 
     * @returns {Promise<void>} 
     */
    onClick(e) {
        return displayPageForUrl(appendQueryParameters(this.targetPageUrl, this.parameters));
    }

    asString() {
        return 'PageLink';
    }


}
