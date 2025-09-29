class ActionButtonHandler extends TagHandler {

    /**
     * @param {Element} element
     * @param {HttpClient} client
     * @param {WidgetContainers} widgetContainers
     */
    constructor(element, client, widgetContainers) {
        super(element);
        this.type = 'action-button-handler';
        this.client = client;
        this.widgetContainers = widgetContainers;
        this.actionParameters = {};
        this.actionExpression = this.variableTextContentFromAttribute('xis:action'); // mandatory
        this.targetContainerId = element.getAttribute('xis:target-container');
        
        // Fix: Set type="button" to prevent form submission
        if (element.tagName.toLowerCase() === 'button' && !element.getAttribute('type')) {
            element.setAttribute('type', 'button');
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
        this.action = this.actionExpression.evaluate(data);
        return Promise.resolve();
    }

    addParameter(name, value) {
        this.actionParameters[name] = value;
    }

    /**
     * @public
     * @param {Event} e 
     */
    onClick(e) {
        const formHandler = this.findParentFormHandler();
        if (formHandler) {
            formHandler.submit(this.action);
        } else {
            const widgetcontainerHandler = this.findParentWidgetContainerHandler();
            const targetContainerHandler = this.targetContainerId ? app.tagHandlers.getHandler(this.widgetContainers.findContainer(this.targetContainerId)) : null;
            if (widgetcontainerHandler || targetContainerHandler) {
                this.widgetAction(widgetcontainerHandler, targetContainerHandler);
            } else {
                this.pageAction();
            }
        }
    }

    widgetAction(widgetcontainerHandler, targetContainerHandler) {
        if (!targetContainerHandler) {
            targetContainerHandler = widgetcontainerHandler;
        }
        this.client.widgetLinkAction(widgetcontainerHandler.widgetInstance, widgetcontainerHandler.widgetState, this.action, this.actionParameters)
            .then(response => this.handleActionResponse(response, targetContainerHandler));
    }

    /**
     * @private
     * @param {string} action 
     */
    pageAction() {
        app.pageController.submitPageLinkAction(this.action, this.actionParameters);
    }

    handleActionResponse(response, targetContainerHandler) {
        if (response.nextURL) {
            app.pageController.handleActionResponse(response);
        } else {
            targetContainerHandler.handleActionResponse(response);
        }
    }

    asString() {
        return 'ActionButton';
    }
}