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
        this.parentForm = this.findParentFormElement();
        element.setAttribute("href", "#");
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
        this.actionParameters = {};
        this.data = data;
        this.refreshDescendantHandlers(data);
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
        if (this.parentForm) {
            this.parentForm.handler.submit(this.action);
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
        var targetContainerHandler = targetContainer.handler;
        var invokerHandler = invokerContainer.handler;
        var _this = this;
        this.client.widgetLinkAction(invokerHandler.widgetInstance, invokerHandler.widgetState, this.action, this.actionParameters)
            .then(response => _this.handleActionResponse(response, targetContainerHandler));
    }

    /**
     * @private
     * @param {string} action 
     */
    pageAction() {
        app.pageController.submitPageLinkAction(this.action, this.actionParameters);
    }


    formAction(form) {
        var handler = form.handler;
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


