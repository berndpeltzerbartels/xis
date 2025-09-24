class ClientStateContentHandler {

    /**
     * 
     * @param {Element} element 
     * @param {ClientState} clientState 
     */
    constructor(element, clientState) {
        this.element = element;
        this.clientState = clientState;
        this.name = "ClientStateContentHandler";
        this.keyExpression = new ExpressionParser(elFunctions).parse(this.element.getAttribute('xis:client-state'));
        app.eventPublisher.subscribe(EventType.CLIENT_STATE_CHANGED, (data) => this.refresh(data));
    }

    refresh(data) {
        var key = this.keyExpression.evaluate(data);
        var value = this.clientState.readValue(key);
        if (value === undefined || value === null) {
            this.element.style.display = 'none';
        } else {
            this.element.style.display = '';
            this.element.innerText = value;
        }
    }
}

