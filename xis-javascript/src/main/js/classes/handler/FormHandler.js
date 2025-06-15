
class FormHandler extends TagHandler {

    /**
     * 
     * @param {Element} formTag 
     * @param {HttpClient} client 
     */
    constructor(formTag, client) {
        super(formTag)
        this.type = 'form-handler';
        this.client = client;
        this.formElementHandlers = {};
        if (!formTag.getAttribute('xis:binding')) {
            throw new Error('form has no binding: ' + this.tag);
        }
        this.bindingExpression = new TextContentParser(formTag.getAttribute('xis:binding'), this).parse();
        formTag.addEventListener('submit', event => event.preventDefault());
    }

    submit(action) {
        var resolvedUrl = app.pageController.resolvedURL;
        var formHandler = this;
        var formBindingParameters = urlParameters(this.binding);
        var formBindingKey = stripQuery(this.binding);
        this.client.formAction(resolvedUrl, this.widgetId(), this.formData(), action, formBindingKey, formBindingParameters).then(response => {
            formHandler.handleActionResponse(response, formHandler.targetContainerHandler());
        });
    }

    formData() {
        var data = {}
        for (var key of Object.keys(this.formElementHandlers)) {
            if (!data[key]) {
                data[key] = [];
            }
            var arr = data[key];
            arr.push(this.formElementHandlers[key].getValue());
        }
        return data;
    }

    widgetId() {
        const handler = this.findParentWidgetContainerHandler();
        if (!handler) {
            return null;
        }
        return handler.currentWidgetId();
    }

    targetContainerHandler() {
        var container = this.findParentWidgetContainer();
        return container ? app.tagHandlers.getHandler(container) : null;
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
        data.validationPath = '/' + formBindingKey;
        this.refreshDescendantHandlers(data);
        this.client.loadFormData(app.pageController.resolvedURL, this.widgetId(), formBindingKey, formBindingParameters)
            .then(response => formHandler.refreshFormData(formHandler.subData(response, formBindingKey)));
    }

    /**
     * Creates a new Data object for embedded for elements from the response
     * @param {ServerResponse} response 
     * @returns 
     */
    subData(response, formBindingKey) {
        var values = response.formData.getValue([formBindingKey]) || {};
        return new Data(values);
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
        if (response.nextWidgetId) {
            targetContainerHandler.handleActionResponse(response);
        } else {
            app.pageController.handleActionResponse(response);
        }
    }
}