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
        this.data = new Data({});
        this.parentForm = this.findParentFormElement();
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
        if (this.parentForm) {
            this.parentForm._handler.submit(this.action);
        } else {
            var widgetcontainer = this.findParentWidgetContainer();
            if (widgetcontainer) {
                this.widgetAction(widgetcontainer);
            } else {
                this.pageAction();
            }
        }
    }

    widgetAction(invokerContainer) {
        var targetContainer = this.targetContainerId ? this.widgetContainers.findContainer(this.targetContainerId) : invokerContainer;
        var targetContainerHandler = targetContainer._handler;
        var invokerHandler = invokerContainer._handler;
        var _this = this;
        this.client.widgetAction(invokerHandler.widgetInstance, invokerHandler.widgetState, this.action)
            .then(response => _this.handleActionResponse(response, targetContainerHandler));
    }

    /**
     * @private
     * @param {string} action 
     */
    pageAction() {
        app.pageController.submitAction(this.action);
    }


    formAction(form) {
        var handler = form._handler;
        handler.sumbit(this.action)
    }


    handleActionResponse(response, targetContainerHandler) {
        if (response.nextPageURL) {
            app.pageController.handleActionResponse(response);
        } else {
            targetContainerHandler.handleActionResponse(response);
        }
    }

    asString() {
        return 'Link';
        // TODO
    }


}


