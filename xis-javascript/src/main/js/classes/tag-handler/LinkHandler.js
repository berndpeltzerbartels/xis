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
        this.pageUrlExpression = this.expressionFromAttribute('xis:page');
        this.widgetUrlExpression = this.expressionFromAttribute('xis:widget');
        this.targetContainerExpression = this.expressionFromAttribute('xis:target-container');
        if (!this.targetContainerExpression) {
            this.parentWidgetContainer = this.findParentWidgetContainer();
        }
        if (element.localName == 'a') {
            element.setAttribute('href', '#');
        }
        element.onclick = e => { this.onClick(e).catch(e => console.error(e)); };
    }

    /**
     * @public
     * @param {Data} data 
     */
    refresh(data) {
        this.data = data;
        if (this.pageUrlExpression) {
            this.targetPageUrl = this.pageUrlExpression.evaluate(data);
        }
        if (this.widgetUrlExpression) {
            this.targetWidgetUrl = this.widgetUrlExpression.evaluate(data);
        }
        if (this.targetContainerExpression) {
            this.targetContainerId = this.targetContainerExpression.evaluate(data);
        }
    }


    /**
     * @public
     * @param {Event} e 
     * @returns {Promise<void>} 
     */
    onClick(e) {
        if (this.widgetUrlExpression) {
            return this.onClickWidgetLink();
        } else if (this.pageUrlExpression) {
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
            var widgetId = this.targetWidgetId();
            handler.showWidget(widgetId, widgetState);
            resolve();
        });
    }

    /**
     * @private
     * @returns {string} the widget-id by leaving query from the widget-url
     */
    targetWidgetId() {
        if (this.targetWidgetUrl.indexOf('?') != -1) {
            return doSplit(this.targetWidgetUrl, '?')[1];
        }
        return this.targetWidgetUrl;
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


