class PageLinkHandler extends TagHandler {

    /**
     * 
     * @param {Element} element 
     * @param {WidgetContainers} widgetContainers
     */
    constructor(element) {
        super(element);
        this.type = 'page-link-handler';
        element.setAttribute("href", "#");
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
        this.data = data;
        const descendantPromise = this.refreshDescendantHandlers(data); // attributes might have variables !
        this.targetPageUrl = this.tag.getAttribute('xis:page');
        this.targetWidgetUrl = this.tag.getAttribute('xis:widget');
        this.targetContainerId = this.tag.getAttribute('xis:target-container');
        return descendantPromise;
    }
    
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
        return 'Link';
        // TODO
    }


}


