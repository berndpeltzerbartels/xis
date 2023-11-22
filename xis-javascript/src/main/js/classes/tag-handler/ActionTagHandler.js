class ActionTagHandler extends TagHandler {
    constructor(tag) {
        super(tag);
    }

    onAction(e) {
        var widgetcontainer = this.findParentWidgetContainer();
        if (widgetcontainer) {
            this.widgetAction(widgetcontainer);
        } else {
            this.pageAction();
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
        app.pageController.submitAction(this.action);
    }

}