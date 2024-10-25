
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

    submit(action, actionParameters) {
        var resolevdUrl = app.pageController.resolvedURL;
        var formHandler = this;
        var formBindingParameters = urlParameters(this.binding);
        var formBindigKey = stripQuery(this.binding);
        this.client.loadFormData(app.pageController.resolvedURL, this.widgetId(), formBindigKey, formBindingParameters);
        //resolvedURL, widgetInstance, formData, action, actionParameters, binding
        this.client.formAction(resolevdUrl, this.widgetId(), this.formData, action, formBindigKey, formBindingParameters).then(response => {
            formHandler.handleActionResponse(response, formHandler.targetContainerHandler());
        });
    }

    widgetId() {
        var container = this.findParentWidgetContainer();
        if (!container) {
            return null;
        }
        return container._handler.currendWidgetId();
    }

    targetContainerHandler() {
        var container = this.findParentWidgetContainer();
        return container ? container._handler : null;
    }

    validate() { }

    /**
     * @public
     * @override
     * @param {Data} data 
     */
    refresh(data) {
        this.binding = this.bindingExpression.evaluate(data);
        var formBindingParameters = urlParameters(this.binding);
        var formBindigKey = stripQuery(this.binding);
        this.client.loadFormData(app.pageController.resolvedURL, this.widgetId(), formBindigKey, formBindingParameters); // TODO
        this.refreshDescendantHandlers(data);

    }

    widgetId() {
        var container = this.findParentWidgetContainer();
        return container ? container._handler.widgetInstance.id : null;
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

    /**
     * @private
     * @param {ServerResponse} response 
     * @param {WidgetContainerHandler} targetContainerHandler 
     */
    handleActionResponse(response, targetContainerHandler) {
        if (response.nextPageURL) {
            app.pageController.handleActionResponse(response);
        } else {
            targetContainerHandler.handleActionResponse(response);
        }
    }
}