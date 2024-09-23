
class FormHandler extends TagHandler {

    /**
     * 
     * @param {Element} formTag 
     * @param {Client} client 
     */
    constructor(formTag, client) {
        super(formTag)
        this.type = 'form-handler';
        this.client = client;
        this.formData = new Data({});
        if (!formTag.getAttribute('xis:binding')) {
            throw new Error('form has no binding: ' + this.tag);
        }
        this.bindingExpression = new TextContentParser(formTag.getAttribute('xis:binding')).parse();
        formTag.addEventListener('submit', event => event.preventDefault());
    }

    submit(action) {
        var widgetcontainer = this.findParentWidgetContainer();
        if (widgetcontainer) {
            this.widgetAction(action, widgetcontainer);
        } else {
            this.pageAction(action);
        }
    }

    validate() { }

    /**
     * @public
     * @override
     * @param {Data} data 
     */
    refresh(data) {
        this.binding = this.bindingExpression.evaluate(data);
        this.refreshDescendantHandlers(data);
    }

    /**
     * @public
     */
    reset() {
        // TODO: implement
    }

    /**
     * 
     * @param {TagHandler} handler 
     * @param {array<String>} bindingPath 
     */
    onElementHandlerRefreshed(handler, bindingPath) {
        this.formData.setValue(bindingPath, new Value(handler.tag));
    }

    widgetAction(action, invokerContainer) {
        var targetContainer = this.targetContainerId ? this.widgetContainers.findContainer(this.targetContainerId) : invokerContainer;
        var targetContainerHandler = targetContainer._handler;
        var invokerHandler = invokerContainer._handler;
        var _this = this;
        this.client.widgetAction(invokerHandler.widgetInstance, invokerHandler.widgetState, action, this.formData, {})
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
    pageAction(action) {
        app.pageController.submitFormAction(action, this.formData);
    }


}