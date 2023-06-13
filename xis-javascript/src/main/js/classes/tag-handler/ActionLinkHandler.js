class ActionLinkHandler extends TagHandler {

    /**
     * 
     * @param {Element} element 
     * @param {Client} client 
     * @param {WidgetContainers} widgetContainers
     */
    constructor(element, client, widgetContainers) {
        super(element);
        this.client = client;
        this.widgetContainers = widgetContainers;
        this.type = 'action-link-handler';
        this.targetExpression = this.expressionFromAttribute('xis:target-container'); // not mandatory
        this.actionExpression = this.expressionFromAttribute('xis:action'); // mandatory
        this.parentWidgetContainer = this.findParentWidgetContainer();
        this.targetContainer = undefined;
        element.onclick = e => this.onClick(e);
        if (element.localName == 'a') {
            element.setAttribute('href', '#');
        }
    }

    refresh(data) {
        if (this.targetExpression) {
            this.targetContainer = this.widgetContainers.findContainer(this.targetExpression.evaluate(data));
        } else {
            this.targetContainer = this.parentWidgetContainer;
        }
        this.action = this.actionExpression.evaluate(data);
    }


    onClick(e) {
        if (this.targetContainer) {
            this.targetContainer._handler.submitAction(this.action);
        } else {
            app.pageController.submitAction(this.action).catch(e => console.error(e));
        }
    }

    getParentContainer() { }

    asString() {
        return 'Link';
        // TODO
    }


}


