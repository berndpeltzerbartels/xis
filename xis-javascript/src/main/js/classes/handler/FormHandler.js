
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
        if (formTag.getAttribute('xis:binding')) {
            this.bindingExpression = new TextContentParser(formTag.getAttribute('xis:binding')).parse();
        }
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
        this.formData = new Data({}, data);
        if (this.bindingExpression) {
            this.binding = this.bindingExpression.evaluate(data);
            this.refreshDescendantHandlers(new Data(data.getValueByPath(this.binding), data));
        } else {
            this.refreshDescendantHandlers(data);
        }
    }

    registerElementHandler(handler) {
        var path = [];
        if (this.bindingExpression) {
            path.push(this.binding);
        }
        for (var part of doSplit(handler.binding, '.')) {
            path.push(part); // array.push(array) fails in GraalVM
        }
        this.formData.setValue(path, new Value(handler.tag));
    }

    widgetAction(action, invokerContainer) {
        var targetContainer = this.targetContainerId ? this.widgetContainers.findContainer(this.targetContainerId) : invokerContainer;
        var targetContainerHandler = targetContainer._handler;
        var invokerHandler = invokerContainer._handler;
        var _this = this;
        this.client.widgetFormAction(invokerHandler.widgetInstance, invokerHandler.widgetState, action, this.formData)
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