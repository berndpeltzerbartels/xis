
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
        this.fileInputHandlers = [];
        this.globalMessageHandlers = [];
        this.messageHandlers = {};
        if (!formTag.getAttribute('xis:binding')) {
            throw new Error('form has no binding: ' + this.tag);
        }
        this.bindingExpression = new TextContentParser(formTag.getAttribute('xis:binding')).parse();
        formTag.addEventListener('submit', event => Promise.resolve(this.onSubmit(event)).catch(error => handleError(error)));
    }

    submit(action, actionParameters = {}) {
        var resolvedUrl = app.pageController.resolvedURL;
        var formBindingParameters = urlParameters(this.binding);
        var formBindingKey = stripQuery(this.binding);
        var parameters = mergeObjects(this.frontletParameters || {}, formBindingParameters);
        this.resetMessageHandlers();
        return this.client.formAction(resolvedUrl, this.frontletId(), this.formData(), action, formBindingKey, parameters, this.modalParameters || {}, actionParameters, this.uploads())
            .then(response => this.handleActionResponse(response, this.targetContainerHandler()));
    }

    onSubmit(event) {
        event.preventDefault();
        const submitter = event.submitter || this.findDefaultSubmitter(this.tag);
        if (!submitter) {
            return Promise.resolve();
        }
        const handler = app.tagHandlers.getHandler(submitter);
        if (handler && handler.action) {
            return this.submit(handler.action, handler.actionParameters || {});
        }
        const action = submitter.getAttribute('xis:action');
        if (action) {
            return this.submit(action);
        }
        return Promise.resolve();
    }

    findDefaultSubmitter(element) {
        for (const child of nodeListToArray(element.childNodes)) {
            if (!isElement(child)) {
                continue;
            }
            if (this.isSubmitter(child)) {
                return child;
            }
            const nested = this.findDefaultSubmitter(child);
            if (nested) {
                return nested;
            }
        }
        return null;
    }

    isSubmitter(element) {
        if (!element.getAttribute('xis:action')) {
            return false;
        }
        const tagName = element.localName;
        return tagName === 'button' || tagName === 'input';
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

    uploads() {
        const uploads = [];
        for (const handler of this.fileInputHandlers) {
            uploads.push(...handler.getUploads());
        }
        return uploads;
    }

    frontletId() {
        const handler = this.findParentFrontletContainerHandler();
        if (!handler) {
            return null;
        }
        return handler.currentFrontletId();
    }

    targetContainerHandler() {
        var container = this.findParentFrontletContainer();
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
        var frontletParameters = data.getValue(['frontletParameters']) || {};
        this.frontletParameters = frontletParameters;
        this.modalParameters = data.getValue(['modalParameters']) || {};
        var formBindingKey = stripQuery(this.binding);
        this.formElementHandlers = {};
        this.fileInputHandlers = [];
        data.validationPath = '/' + formBindingKey;
        this.resetMessageHandlers();
        this.clearMessageHandlers();
        const descendantPromise = this.refreshDescendantHandlers(data);
        const actionFormData = this.actionFormData(data, formBindingKey);
        const formDataPromise = actionFormData !== undefined
            ? Promise.resolve(this.refreshFormData(new Data(actionFormData || {})))
            : this.client.loadFormData(app.pageController.resolvedURL, this.frontletId(), formBindingKey, frontletParameters, this.modalParameters, this.formDataLoad(data))
                .then(response => this.refreshFormData(this.subData(response, formBindingKey)));
        return Promise.all([descendantPromise, formDataPromise]);
    }

    formDataLoad(data) {
        return data.load || 'INITIAL';
    }

    actionFormData(data, formBindingKey) {
        if (!data.actionFormDataKeys || !data.actionFormDataKeys.includes(formBindingKey) || !data.actionFormData) {
            return undefined;
        }
        return data.actionFormData.getValue([formBindingKey]);
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
     * @param {FrontletContainerHandler} targetContainerHandler
     */
    handleActionResponse(response, targetContainerHandler) {
        this.refreshValidatorMessages(response.validatorMessages);
        if (!response.validatorMessages.isEmpty()) {
            return Promise.resolve();
        }
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
            if (!binding) {
                for (const handlers of Object.values(this.messageHandlers)) {
                    handlers.forEach(handler => handler.reset());
                }
            }
            return;
        }
        this.messageHandlers[binding].forEach(handler => handler.reset());
    }
}
