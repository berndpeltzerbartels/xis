class StoreBindingHandler extends TagHandler {
    constructor(tag) {
        super(tag);
        const bindingAttribute = this.tag.getAttribute('binding');
        if (!bindingAttribute) {
            throw new Error("StoreBindingHandler: 'binding' attribute is required.");
        }
        this.varName = this.getAttribute('var');
        this.bindingExpression = this.createExpression(this.getAttribute('binding'));
        this.storeNameExpression = this.createExpression(this.getAttribute('store'));

        this.storeName = null;
        this.binding = null;
    }


    refresh(data) {
        const eventPublisher = app.eventPublisher;

        if (this.storeName && this.listener) {
            eventPublisher.removeEventListener(this.storeName + 'Updated', this.listener);
        }

        this.data = data;
        this.storeName = this.storeNameExpression.evaluate(data);;
        this.binding = this.bindingExpression.evaluate(data);

        const dotIndex = this.binding.indexOf('.');
        let key;
        let path;
        if (dotIndex === -1) {
           key = this.binding;
           path = null;
       } else {
           key = this.binding.substring(0, dotIndex);
           path = this.binding.substring(dotIndex + 1);
       }

        this.validateStoreName(this.storeName);

        this.listener = event => {
            if (event.key === key) {
                this.handleUpdateEvent(event, path);
            }
        };

        eventPublisher.addEventListener(this.storeName + 'Updated', this.listener);
        this.refreshDescendantHandlers(data, path);
    }


    handleUpdateEvent(event, path) {
    debugger;
        const eventData = new Data(event.value, this.data);
        const value = path ? eventData.getValue(doSplit(path)) : event.value;
        const key = this.varName;
        const dataToProvide = new Data({}, this.data);
        dataToProvide.setValue([key], value);
        this.refreshDescendantHandlers(dataToProvide);
    }

    validateStoreName(storeName) {
        const validStoreNames = ['localStorage', 'sessionStorage', 'clientStorage'];
        if (!validStoreNames.includes(storeName)) {
            throw new Error(`Invalid store name: ${storeName}. Valid names are: ${validStoreNames.join(', ')}`);
        }
    }
}