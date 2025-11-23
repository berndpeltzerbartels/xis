class ActionLinkHandler extends TagHandler {

    /**
     * @param {Element} element
     * @param {HttpClient} client
     * @param {WidgetContainers} widgetContainers
     */
    constructor(element, client, widgetContainers) {
        super(element);
        this.client = client;
        this.widgetContainers = widgetContainers;
        this.type = 'action-link-handler';
        this.targetContainerId = undefined;
        this.action = undefined;
        this.data = new Data({});
        this.actionParameters = {};
        element.setAttribute("href", "#");
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
        return this.refreshDescendantHandlers(data).then(() => {
            this.refreshWithData(data);
        });
    }


    /**
     * @public
     */
    reapply() {
        return this.reapplyDescendantHandlers().then(() => {
            this.refreshWithData(this.data);
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
     * @returns {Promise<void>}
     */
    pageAction() {
        return app.pageController.submitPageLinkAction(this.action, this.actionParameters);
    }


    formAction(form) {
        var handler = app.tagHandlers.getHandler(form);
        if (!handler) {
            throw new Error('no form handler for ' + form);
        }
        handler.sumbit(this.action)
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
        return 'Link';
        // TODO
    }


}


