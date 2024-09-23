class CompositeTagHandler {

    constructor(element) {
        this.element = element;
        this.handlers = [];
        this.type = 'composite-tag-handler';
    }

    addHandler(handler) {
        this.handlers.push(handler);
    }

    refresh(data, formData) {
        this.handlers.forEach(handler => handler.refresh(data, formData));
    }

    showWidget(widgetId, widgewtState) {
        var handler = this.widgetContainerHandler();
        if (handler) {
            handler.showWidget(widgetId, widgewtState);
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