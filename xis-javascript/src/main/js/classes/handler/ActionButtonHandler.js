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
     * @returns {Promise}
     */
    refresh(data) {
        this.data = data;
        return this.renderWithData(data);
    }


    /**
     * @public
     */
    reapply() {
        return this.renderWithData(this.data);
    }


    /**
     * @private
     * @param {Data} data
     * @returns {Promise}
     */
    renderWithData(data) {
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
            // if taget container is not set explicitly, use the parent container
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
        switch (response.actionProcessing) {
            case 'NONE':
                break;
            case 'PAGE':
                app.pageController.handleActionResponse(response);
                break;
            case 'WIDGET':
                if (targetContainerHandler) {
                    targetContainerHandler.handleActionResponse(response);
                }
                break;
            default:
                throw new Error('Unknown action processing type: ' + response.actionProcessing);
        }
    }

    asString() {
        return 'ActionButton';
    }
}