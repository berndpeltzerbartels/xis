
class FormHandler extends TagHandler {

    /**
     * 
     * @param {Element} formTag 
     * @param {Client} client 
     */
    constructor(formTag, client) {
        super(formTag)
        this.client = client;
        this.action = formTag.getAttribute('xis:action');
        this.formData = new Data({});
        this.pathExpr = new TextContentParser().parse(this.getAttribute('form-data'));
        var _this = this;
        formTag.addEventListener('submit', event => {
            event.preventDefault();
            _this.submit(event);
        });

    }

    submit(event) {
        var widgetcontainer = this.findParentWidgetContainer();
        if (widgetcontainer) {
            this.widgetAction(widgetcontainer);
        } else {
            this.pageAction();
        }
    }

    refresh(data) {
        var formDataPath = doSplit(this.pathExpr.evaluate(data));
        var path = doSplit(formDataPath, '.');
        this.actionElements = {};
        this.formData = new Data(data.getValue(path));
        this.refreshChildNodes(data);
    }


    registerFormElement(formElement) {
        this.formElements.push(formElement);
    }

    registerActionElement(name, element) {
        this.actionElements[name] = element;
    }

    validateSendAttribute(value) {
        switch (value) {
            case 'never':
            case 'onsubmit':
            case 'onkey':
            case 'onkeyup':

        }
    }


    widgetAction(invokerContainer) {
        var targetContainer = this.targetContainerId ? this.widgetContainers.findContainer(this.targetContainerId) : invokerContainer;
        var targetContainerHandler = targetContainer._handler;
        var invokerHandler = invokerContainer._handler;
        var _this = this;
        this.client.widgetFormAction(invokerHandler.widgetInstance, invokerHandler.widgetState, this.action, this.formData)
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
    pageAction() {
        app.pageController.submitFormAction(this.action, this.formData);
    }


}