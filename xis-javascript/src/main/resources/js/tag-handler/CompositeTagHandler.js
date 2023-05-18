class CompositeTagHandler {

    constructor(element) {
        this.element = element;
        this.handlers = [];
        this.type = 'composite-tag-handler';
    }

    addHandler(handler) {
        this.handlers.push(handler);
    }

    refresh(data) {
        this.handlers.forEach(handler => handler.refresh(data));
    }

    showWidget(widgetId) {
        var handler = this.widgetContainerHandler();
        if (handler) {
            handler.showWidget(widgetId);
        }
    }


    widgetContainerHandler() {
        for (var handler of this.handlers) {
            if (handler.type == 'widget-container-handler') {
                return handler;
            }
        }
    }

}