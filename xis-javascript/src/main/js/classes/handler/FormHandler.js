
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
        this.pathExpr = new TextContentParser(formTag.getAttribute('xis:binding')).parse();
        var _this = this;
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
        var formDataPath = this.pathExpr.evaluate(data);
        this.actionElements = {};
        this.formData = new Data(data.getValueByPath(formDataPath));
        this.refreshDescendantHandlers(data);
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