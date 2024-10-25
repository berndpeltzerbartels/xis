
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
        this.formElementHandlers = {};
        if (!formTag.getAttribute('xis:binding')) {
            throw new Error('form has no binding: ' + this.tag);
        }
        this.bindingExpression = new TextContentParser(formTag.getAttribute('xis:binding')).parse();
        formTag.addEventListener('submit', event => event.preventDefault());
    }

    submit(action) {
        var resolevdUrl = app.pageController.resolvedURL;
        var formHandler = this;
        var formBindingParameters = urlParameters(this.binding);
        var formBindingKey = stripQuery(this.binding);
        this.client.formAction(resolevdUrl, this.widgetId(), this.formData(), action, formBindingKey, formBindingParameters).then(response => {
            formHandler.handleActionResponse(response, formHandler.targetContainerHandler());
        });
    }

    formData() {
        var data = {}
        for (var key of Object.keys(this.formElementHandlers)) {
            data[key] = this.formElementHandlers[key].getValue();
        }
        return data;
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

    /**
     * @public
     * @override
     * @param {Data} data 
     */
    refresh(data) {
        this.binding = this.bindingExpression.evaluate(data);
        var formBindingParameters = urlParameters(this.binding);
        var formBindingKey = stripQuery(this.binding);
        var formHandler = this;
        this.formElementHandlers = {};
        this.refreshDescendantHandlers(data);
        this.client.loadFormData(app.pageController.resolvedURL, this.widgetId(), formBindingKey, formBindingParameters)
            .then(response => formHandler.refreshFormData(response.formData));
    }

    widgetId() {
        var container = this.findParentWidgetContainer();
        return container ? container._handler.widgetInstance.id : null;
    }

    /**
     * 
     * @param {TagHandler} handler 
     * @param {String} binding
     */
    onElementHandlerRefreshed(handler, binding) {
        this.formElementHandlers[binding] = handler;
    }

    /**
     * @private
     * @param {ServerResponse} response 
     * @param {WidgetContainerHandler} targetContainerHandler 
     */
    handleActionResponse(response, targetContainerHandler) {
        this.refreshValidatorMessages(response.validatorMessages);
        if (!response.validatorMessages.isEmpty()) {
            return;
        }
        if (response.nextPageURL) {
            app.pageController.handleActionResponse(response);
        } else {
            targetContainerHandler.handleActionResponse(response);
        }
    }
}