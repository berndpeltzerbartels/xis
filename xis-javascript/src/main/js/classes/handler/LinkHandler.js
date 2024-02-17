class LinkHandler extends TagHandler {

    /**
     * 
     * @param {Element} element 
     * @param {WidgetContainers} widgetContainers
     */
    constructor(element, widgetContainers) {
        super(element);
        this.type = 'link-handler';
        this.widgetContainers = widgetContainers;
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
        this.refreshDescendantHandlers(data); // attributes might have variables !
        this.targetPageUrl = this.tag.getAttribute('xis:page');
        this.targetWidgetUrl = this.tag.getAttribute('xis:widget');
        this.targetContainerId = this.tag.getAttribute('xis:target-container');
    }


    /**
     * @public
     * @param {Event} e 
     * @returns {Promise<void>} 
     */
    onClick(e) {
        if (this.targetWidgetUrl) {
            return this.onClickWidgetLink();
        } else if (this.targetPageUrl) {
            return this.onClickPageLink();
        }
    }

    /**
     * @private
     * @returns {Promise<void>} 
     */
    onClickWidgetLink() {
        return new Promise((resolve, _) => {
            var container = this.getTargetContainer();
            var handler = container._handler;
            var widgetParameters = urlParameters(this.targetWidgetUrl);
            var widgetState = new WidgetState(app.pageController.resolvedURL, widgetParameters);
            var widgetId = stripQuery(this.targetWidgetUrl);
            handler.showWidget(widgetId, widgetState);
            resolve();
        });
    }

    /**
     * @private
     * @returns {Promise<void>}
     */
    onClickPageLink() {
        return displayPageForUrl(this.targetPageUrl);
    }

    /**
     * @private
     * @returns {Element}
     */
    getTargetContainer() {
        if (this.targetContainerId) {
            return this.widgetContainers.findContainer(this.targetContainerId);
        }
        return this.findParentWidgetContainer();
    }




    asString() {
        return 'Link';
        // TODO
    }


}


