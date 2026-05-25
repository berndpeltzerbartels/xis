class ActionButtonHandler extends TagHandler {

    /**
     * @param {Element} element
     * @param {HttpClient} client
     * @param {FrontletContainers} frontletContainers
     */
    constructor(element, client, frontletContainers) {
        super(element);
        this.type = 'action-button-handler';
        this.client = client;
        this.frontletContainers = frontletContainers;
        this.actionParameters = {};
        this.actionExpression = this.variableTextContentFromAttribute('xis:action'); // mandatory
        this.targetContainerId = element.getAttribute('xis:target-container');

        // Fix: Set type="button" to prevent form submission
        if (element.tagName.toLowerCase() === 'button' && !element.getAttribute('type')) {
            element.setAttribute('type', 'button');
        }

        element.addEventListener('click', event => {
            event.preventDefault();
            return Promise.resolve(this.onClick(event)).catch(error => handleError(error));
        });
    }

    /**
     * @public
     * @param {Data} data 
     * @returns {Promise}
     */
    refresh(data) {
        this.data = data;
        this.renderWithData(data);
        return this.refreshDescendantHandlers(data);
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
            return formHandler.submit(this.action, this.actionParameters);
        } else {
            const frontletContainerHandler = this.findParentFrontletContainerHandler();
            const targetContainerHandler = this.targetContainerId ? app.tagHandlers.getHandler(this.frontletContainers.findContainer(this.targetContainerId)) : null;
            if (frontletContainerHandler) {
                return this.frontletAction(frontletContainerHandler, targetContainerHandler);
            } else {
                return this.pageAction(targetContainerHandler);
            }
        }
    }

    frontletAction(frontletContainerHandler, targetContainerHandler) {
        if (!targetContainerHandler) {
            // if target container is not set explicitly, use the parent container
            targetContainerHandler = frontletContainerHandler;
        }
        return this.client.frontletLinkAction(frontletContainerHandler.frontletInstance, frontletContainerHandler.frontletState, this.action, this.actionParameters)
            .then(response => this.handleActionResponse(response, targetContainerHandler));
    }

    /**
     * @private
     * @param {string} action 
     */
    pageAction(targetContainerHandler) {
        return this.client.pageLinkAction(app.pageController.resolvedURL, this.action, this.actionParameters)
            .then(response => this.handleActionResponse(response, targetContainerHandler));
    }

    handleActionResponse(response, targetContainerHandler) {
        switch (response.actionProcessing) {
            case 'NONE':
                return Promise.resolve();
            case 'PAGE':
                return app.pageController.handleActionResponse(response);
            case 'FRONTLET':
                if (targetContainerHandler) {
                    return targetContainerHandler.handleActionResponse(response);
                }
                return Promise.resolve();
            case 'MODAL':
                return app.modals.handleActionResponse(response, targetContainerHandler);
            default:
                throw new Error('Unknown action processing type: ' + response.actionProcessing);
        }
    }

    asString() {
        return 'ActionButton';
    }
}
