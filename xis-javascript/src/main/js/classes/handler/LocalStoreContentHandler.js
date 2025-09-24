class LocalStoreContentHandler {

    constructor(element, localStore) {
        this.element = element;
        this.localStore = localStore;
        this.name = "localStoreContentHandler";
        this.keyExpression = new ExpressionParser(elFunctions).parse(this.element.getAttribute('xis:local-storage'));
        app.eventPublisher.subscribe(EventType.CLIENT_STATE_CHANGED, (data) => this.refresh(data));
    }

    refresh(data) {
        var key = this.keyExpression.evaluate(data);
        var value = this.localStore.readValue(key);
        if (value === undefined || value === null) {
            this.element.style.display = 'none';
        } else {
            this.element.style.display = '';
            this.element.innerText = value;
        }
    }
}

