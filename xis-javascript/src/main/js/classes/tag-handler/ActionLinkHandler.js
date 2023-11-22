class ActionLinkHandler extends ActionTagHandler {

    /**
     * @param {Element} element
     * @param {Client} client
     * @param {WidgetContainers} widgetContainers
     */
    constructor(element, client, widgetContainers) {
        super(element);
        this.client = client;
        this.widgetContainers = widgetContainers;
        this.type = 'action-link-handler';
        this.targetContainerId = undefined;
        this.targetContainerExpression = this.expressionFromAttribute('xis:target-container'); // not mandatory
        this.actionExpression = this.expressionFromAttribute('xis:action'); // mandatory
        this.action = undefined;
        this.data = new Data({});
        element.onclick = e => this.onClick(e);
        if (element.localName == 'a') {
            element.setAttribute('href', '#');
        }
    }

    /**
     * @public
     * @param {Data} data 
     */
    refresh(data) {
        this.data = data;
        if (this.targetContainerExpression) {
            this.targetContainerId = this.targetContainerExpression.evaluate(data);
        }
        this.action = this.actionExpression.evaluate(data);
    }

    /**
     * @public
     * @param {Event} e 
     */
    onClick(e) {
        this.onAction(e);
    }

    asString() {
        return 'Link';
        // TODO
    }


}


