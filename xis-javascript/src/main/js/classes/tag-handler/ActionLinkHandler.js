class ActionLinkHandler extends TagHandler {

    /**
     * 
     * @param {Element} element 
     * @param {Client} client 
     * @param {WidgetContainers} widgetContainers
     */
    constructor(element, client, widgetContainers) {
        super(element);
        this.client = client;
        this.widgetContainers = widgetContainers;
        this.type = 'action-link-handler';
        this.targetContainerId = undefined;
        this.targetContainerExpression = this.expressionFromAttribute('xis:target-container'); // not mandatory
        this.actionExpression = this.expressionFromAttribute('xis:action'); // mandatory
        this.action = undefined;
        this.data = {};
        this.widgetId = this.getWidgetId();
        element.onclick = e => this.onClick(e);
        if (element.localName == 'a') {
            element.setAttribute('href', '#');
        }
    }

    /**
     * @public
     * @param {Data} data 
     */
    refresh(data) {
        this.data = data;
        if (this.targetContainerExpression) {
            this.targetContainerId = this.widgetContainers.findContainer(this.targetContainerExpression.evaluate(data));
        }
        this.action = this.actionExpression.evaluate(data);
    }

    /**
     * @public
     * @param {Event} e 
     */
    onClick(e) {
        if (this.widgetId) {
            var targetContainer;
            if (this.targetContainerId) {
                targetContainer = this.widgetContainers.findContainer(this.targetContainerId);
            } else {
                targetContainer = this.findParentWidgetContainer();
            }
            this.client.widgetAction(this.widgetId, this.action, this.data);
        } else {

        }
        var parentWidget = this.findParentWidgetContainer();
        if (parentWidget) {
            this.client.widgetAction()
        } else {

        }

        if (this.targetContainer) {
            this.targetContainer._handler.submitAction(this.action);
        } else {
            app.pageController.submitAction(this.action).catch(e => console.error(e));
        }
    }


    asString() {
        return 'Link';
        // TODO
    }


}


