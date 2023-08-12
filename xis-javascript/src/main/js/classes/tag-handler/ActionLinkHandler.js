class ActionLinkHandler extends TagHandler {

    /**
     * @param {Element} element 
     * @param {WidgetContainers} widgetContainers
     */
    constructor(element, widgetContainers) {
        super(element);
        this.widgetContainers = widgetContainers;
        this.type = 'action-link-handler';
        this.targetContainerId = undefined;
        this.targetContainerExpression = this.expressionFromAttribute('xis:target-container'); // not mandatory
        this.actionExpression = this.expressionFromAttribute('xis:action'); // mandatory
        this.action = undefined;
        this.data = {};
        this.parameters = [];
        this.widgetId = this.getWidgetId();
        element.onclick = e => this.onClick(e);
        if (element.localName == 'a') {
            element.setAttribute('href', '#');
        }
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
        this.data = data;
        if (this.targetContainerExpression) {
            this.targetContainerId = this.targetContainerExpression.evaluate(data);
        }
        this.action = this.actionExpression.evaluate(data);
    }

    /**
     * @public
     * @param {Event} e 
     */
    onClick(e) {
        if (this.widgetId) {
            var widgetContainer = this.findParentWidgetContainer();
            if (this.targetContainerId) {
                widgetContainer._handler.submitAction(this.action, this.parameters, this.targetContainerId);
            } else {
                var targetContainerId = widgetContainer._handler.containerId;
                widgetContainer._handler.submitAction(this.action, this.parameters, targetContainerId);
            }
        } else {
            app.pageController.submitAction(this.action, this.parameters);
        }
    }


    getTargetContainer() {
        if (this.targetContainerId) {
            return this.widgetContainers.findContainer(this.targetContainerId);
        } else {
            return this.findParentWidgetContainer();
        }
    }


    asString() {
        return 'Link';
        // TODO
    }


}


