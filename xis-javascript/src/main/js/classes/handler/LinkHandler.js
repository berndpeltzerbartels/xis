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
        if (this.pageUrlExpression) {
            this.targetPageUrl = this.pageUrlExpression.evaluate(data);
        }
        if (this.widgetUrlExpression) {
            this.targetWidgetUrl = this.widgetUrlExpression.evaluate(data);
        }
        if (this.targetContainerExpression) {
            this.targetContainerId = this.targetContainerExpression.evaluate(data);
        }
        this.refreshDescendantHandlers(data);
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


