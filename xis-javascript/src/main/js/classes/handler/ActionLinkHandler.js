class ActionLinkHandler extends TagHandler {

    /**
     * @param {Element} element
     * @param {HttpClient} client
     * @param {FrontletContainers} frontletContainers
     */
    constructor(element, client, frontletContainers) {
        super(element);
        this.client = client;
        this.frontletContainers = frontletContainers;
        this.type = 'action-link-handler';
        this.targetContainerId = undefined;
        this.action = undefined;
        this.data = new Data({});
        this.actionParameters = {};
        element.setAttribute("href", "#");
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
        this.actionParameters = {};
        return this.refreshDescendantHandlers(data).then(() => {
            this.refreshWithData(data);
            this.applyActionQueryParameters();
        });
    }

    /**
     * @private
     * @param {Data} data
     */
    refreshWithData(data) {
        this.data = data;
        this.targetContainerId = this.tag.getAttribute('xis:target-container');
        this.action = this.tag.getAttribute('xis:action');
    }

    applyActionQueryParameters() {
        var queryParameters = urlParameters(this.action);
        for (var key of Object.keys(queryParameters)) {
            this.actionParameters[key] = queryParameters[key];
        }
        this.action = stripQuery(this.action);
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
            targetContainerHandler = frontletContainerHandler;
        }
        return this.client.frontletLinkAction(frontletContainerHandler.frontletInstance, frontletContainerHandler.frontletState, this.action, this.actionParameters)
            .then(response => this.handleActionResponse(response, targetContainerHandler));
    }

    /**
     * @private
     * @returns {Promise<void>}
     */
    pageAction(targetContainerHandler) {
        return this.client.pageLinkAction(app.pageController.resolvedURL, this.action, this.actionParameters)
            .then(response => this.handleActionResponse(response, targetContainerHandler));
    }


    formAction(form) {
        var handler = app.tagHandlers.getHandler(form);
        if (!handler) {
            throw new Error('no form handler for ' + form);
        }
        return handler.submit(this.action, this.actionParameters);
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
        return 'Link';
        // TODO
    }


}
