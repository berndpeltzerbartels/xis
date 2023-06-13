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
        this.actionExpression = this.expressionFromAttribute('xis:action');
        this.targetContainerId = this.findParentWidgetContainer();
        element.onclick = e => this.onClick(e);
        if (element.localName == 'a') {
            element.setAttribute('href', '#');
        }
    }

    refresh(data) {
        var _this = this;
        if (this.targetExpression) {
            this.targetContainerId = this.targetExpression.evaluate(data);
        }
        if (this.actionExpression) {
            this.action = this.actionExpression.evaluate(data);
        }

    }


    onClick(e) {
        if (this.targetContainerId) {
            var targetContainer = this.widgetContainers.findContainer(this.targetContainerId);
            targetContainer._handler.submitAction(this.action);
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


