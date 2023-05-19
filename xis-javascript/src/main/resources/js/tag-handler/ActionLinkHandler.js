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
        this.targetExpression = this.expressionFromAttribute('target-container'); // not mandatory
        this.actionExpression = this.expressionFromAttribute('action');
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
        this.element.onclick = e => _this.onClick(e);
    }


    onClick(e) {
        if (this.targetContainerId) {
            var targetContainer = this.getTargetContainer(this.targetContainerId);
            targetContainer._handler.submitAction(this.action);
        } else {
            pageController.submitAction(action);
        }
    }

    asString() {
        return 'Link';
        // TODO
    }


}


