class StorageBindingHandler extends TagHandler {
    constructor(tag) {
        super(tag);
        this.type = 'store-listener-handler';
        this.storeNameExpression = this.createExpression(this.getAttribute('store')); // May be dynamic
    }


    refresh(data) {
        const eventPublisher = app.eventPublisher;
        if (this.storeName && this.listener) {
            eventPublisher.removeEventListener(this.listener);
        }
        this.data = data;
        this.storeName = this.storeNameExpression.evaluate(data);
        this.validateStoreName(this.storeName);

        this.listener = event => this.refreshDescendantHandlers(this.data);
        eventPublisher.addEventListener(this.storeName + 'Updated', this.listener);

        this.refreshDescendantHandlers(data);
    }

    validateStoreName(storeName) {
        const validStoreNames = ['localStorage', 'sessionStorage', 'clientStorage'];
        if (!validStoreNames.includes(storeName)) {
            throw new Error(`Invalid store name: ${storeName}. Valid names are: ${validStoreNames.join(', ')}`);
        }
    }
}