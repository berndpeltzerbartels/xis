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
        this.pageIdExpression = this.expressionFromAttribute('xis:page');
        this.widgetIdExpression = this.expressionFromAttribute('xis:widget');
        this.targetContainerExpression = this.expressionFromAttribute('xis:target-container');
        this.parameters = [];
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
     * @param {Parameter} parameter 
     */
    addParameter(parameter) {
        this.parameters.push(parameter);
    }

    /**
     * @public
     * @param {Data} data 
     */
    refresh(data) {
        var _this = this; // TODO validate attributes in backend
        this.data = data;
        if (this.pageIdExpression) {
            this.targetPageId = this.pageIdExpression.evaluate(data);
        }
        if (this.widgetIdExpression) {
            this.targetWidgetId = this.widgetIdExpression.evaluate(data);
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
        if (this.widgetIdExpression) {
            return this.onClickWidgetLink();
        } else if (this.pageIdExpression) {
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
            handler.showWidget(this.targetWidgetId, this.parameters);
            resolve();
        });
    }

    /**
     * @private
     * @returns {Promise<void>}
     */
    onClickPageLink() {
        return displayPage(this.targetPageId, this.parameters);
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


