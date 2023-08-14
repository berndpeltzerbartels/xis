class ActionLinkHandler extends TagHandler {

    /**
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
        this.parameters = {};
        this.widgetId = this.getWidgetId();
        element.onclick = e => this.onClick(e);
        if (element.localName == 'a') {
            element.setAttribute('href', '#');
        }
    }

    /**
   * @public
   * @param {string} name
   * @param {any} value 
   */
    addParameter(name, value) {
        this.parameters[name] = value;
    }
    /**
     * @public
     * @param {Data} data 
     */
    refresh(data) {
        this.parameters = {};
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
            this.widgetAction();
        } else {
            this.pageAction();
        }
    }

    widgetAction() {
        var invokerContainer = this.findParentWidgetContainer();
        var targetContainer = this.targetContainerId ? this.widgetContainers.findContainer(this.targetContainerId) : invokerContainer;
        var targetContainerHandler = targetContainer._handler;
        var targetContainerId = targetContainerHandler.containerId;
        var invokerWidget = invokerContainer._handler.widget;
        var clientData = invokerWidget.clientDataForActionRequest(this.action, this.parameters, targetContainerId);
        var _this = this;
        this.client.widgetAction(invokerWidget.id, clientData, this.action)
            .then(response => _this.handleActionResponse(response, targetContainerHandler));
    }

    handleActionResponse(response, targetContainerHandler) {
        if (response.nextPageURL) {
            app.pageController.handleActionResponse(response);
        } else {
            targetContainerHandler.handleActionResponse(response);
        }
    }

    /**
     * @private
     * @param {string} action 
     */
    pageAction() {
        app.pageController.submitAction(this.action, this.parameters);
    }


    asString() {
        return 'Link';
        // TODO
    }


}


