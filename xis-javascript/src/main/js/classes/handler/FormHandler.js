
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
        this.formElementHandlers = {};
        data.validationPath = '/' + formBindingKey;
        this.clearMessageHandlers();
        this.refreshDescendantHandlers(data);
        app.backendService.loadFormData(app.pageController.resolvedURL, this.widgetId(), formBindingKey, formBindingParameters, this)
            .then(response => this.refreshFormData(this.subData(response, formBindingKey)));
    }

    /**
     * State refresh - updates form with current state data without reloading from backend.
     * This prevents infinite recursion during reactive state updates.
     * @public
     * @param {Data} data - Current state data
     * @param {TagHandler} invoker - The handler that initiated the state refresh
     */
    stateRefresh(data, invoker) {
        // Anti-recursion: Skip if we triggered the state refresh
        if (this === invoker) {
            return;
        }
        
        // Only refresh form with current data, don't reload from backend
        this.refreshDescendantHandlers(data);
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
        this.resetMessageHandlers();
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
        
        // Trigger reactive state updates with this FormHandler as the invoker
        // This ensures the anti-recursion logic stops at this level
        app.backendService.triggerReactiveStateUpdates(response, this);
        
        if (response.nextWidgetId) {
            targetContainerHandler.handleActionResponse(response);
        } else {
            app.pageController.handleActionResponse(response);
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

    resetMessageHandlers() {
        this.globalMessageHandlers.forEach(handler => handler.reset());
        for (var binding of Object.keys(this.messageHandlers)) {
            this.messageHandlers[binding].forEach(handler => handler.reset());
        }
    }
}