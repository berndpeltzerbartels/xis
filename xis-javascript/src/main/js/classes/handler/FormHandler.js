
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
        this.globalMessageHandlers = [];
        this.messageHandlers = {};
        if (!formTag.getAttribute('xis:binding')) {
            throw new Error('form has no binding: ' + this.tag);
        }
        this.bindingExpression = new TextContentParser(formTag.getAttribute('xis:binding')).parse();
        formTag.addEventListener('submit', event => event.preventDefault());
    }

    submit(action) {
        var resolvedUrl = app.pageController.resolvedURL;
        var formBindingParameters = urlParameters(this.binding);
        var formBindingKey = stripQuery(this.binding);
        this.client.formAction(resolvedUrl, this.widgetId(), this.formData(), action, formBindingKey, formBindingParameters).then(response => {
            this.handleActionResponse(response, this.targetContainerHandler());
        });
    }

    formData() {
        const data = {};
        for (const key of Object.keys(this.formElementHandlers)) {
            const handlers = this.formElementHandlers[key];
            // Sammle alle Werte der Handler für diesen Key
            const values = handlers.map(h => h.getValue()).filter(v => v !== undefined && v !== null);
            // Für Checkboxen: Array, für andere Felder: Einzelwert
            data[key] = (values.length > 1) ? values : (values[0] !== undefined ? values[0] : null);
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
        debugger;
        var container = this.findParentWidgetContainer();
        return container ? app.tagHandlers.getHandler(container) : null;
    }

    /**
     * @public
     * @override
     * @param {Data} data 
     */
    refresh(data) {
        this.data = data;
        this.binding = this.bindingExpression.evaluate(data);
        var widgetParameters = data.getValue(['widgetParameters']) || {};
        var formBindingKey = stripQuery(this.binding);
        this.formElementHandlers = {};
        data.validationPath = '/' + formBindingKey;
        this.clearMessageHandlers();
        debugger;
        const descendantPromise = this.refreshDescendantHandlers(data);
        const formDataPromise = this.client.loadFormData(app.pageController.resolvedURL, this.widgetId(), formBindingKey, widgetParameters, this)
            .then(response => this.refreshFormData(this.subData(response, formBindingKey)));
        return Promise.all([descendantPromise, formDataPromise]);
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
        if (!this.formElementHandlers[binding]) {
            this.formElementHandlers[binding] = [];
        }
        this.formElementHandlers[binding].push(handler);
    }

    onFormValueChanges(handler) {
        handler.errorBinding && this.resetMessageHandlers(handler.errorBinding);
    }

    onMessageHandlerRefreshed(handler, binding) {
        if (!this.messageHandlers[binding]) {
            this.messageHandlers[binding] = [];
        }
        this.messageHandlers[binding].push(handler);
    }


    onGlobalMessageHandlerRefreshed(handler) {
        this.globalMessageHandlers.push(handler);
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


    refreshValidatorMessages(validatorMessages) {
        for (var binding of Object.keys(validatorMessages.messages)) {
            if (this.messageHandlers[binding]) {
                for (var handler of this.messageHandlers[binding]) {
                    handler.refreshValidatorMessages(validatorMessages);
                }
            }
        }
        for (var handler of this.globalMessageHandlers) {
            handler.refreshValidatorMessages(validatorMessages);
        }
    }

    /**
    * Clears all message handlers.
    * @private
    */
    clearMessageHandlers() {
        this.globalMessageHandlers = [];
        this.messageHandlers = {};
    }

    resetMessageHandlers(binding) {
        this.globalMessageHandlers.forEach(handler => handler.reset());
        if (!binding || !this.messageHandlers[binding]) {
            return;
        }
        this.messageHandlers[binding].forEach(handler => handler.reset());
    }
}