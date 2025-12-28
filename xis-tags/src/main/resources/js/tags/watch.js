class WatchTagHandler extends TagHandler {

    constructor() {
        super();
        this.type = "watch-tag-handler";
        this.data = undefined;
        this.localStorageAdapter = app.localStorage;
        this.keyExpression = this.expressionFromAttribute("key");
        this.typeExpression = this.expressionFromAttribute("type");
        this.key = undefined;
        app.eventPublisher.addEventListener("storageChanged", this);
    }

    refresh(data) {
        this.data = data;
        this.refreshDescendantHandlers(data);
        this.type = this.typeExpression.evaluate(data);
        this.key = this.keyExpression.evaluate(data);
        if (this.type !== 'local' && this.type !== 'session' && this.type !== 'global') {
            throw new Error("One of 'local', 'session' or 'global' expected, but it was '"+this.type+"'");
        }
    }

    onStorageChanged(event) {
        if (event.key === this.key && event.type === this.type) {
            this.refresh(this.data);
        }
    }
}


class WatchTagConfig  {
    constructor() {
        this.type = "watch-tag-config";
        this.tagHandlerClass = WatchTagHandler;
        this.domAccessor = new DomAccessor();
    }

    matches(element) {
        if (element.tagName.toLowerCase() === "xis:watch") {
            return true;
        }
        if (element.getAttribute("xis:watch")) {
            return true;
        }
        return false;
    }

    normalize(element) {
        if (element.tagName.toLowerCase() === "xis:watch") {
            return element;
        }
        const watchValue = element.getAttribute("xis:watch");
        const typeValue = element.getAttribute("xis:type");

        element.removeAttribute("xis:watch");
        element.removeAttribute("xis:type");

        const watchElement = document.createElement("xis:watch");
        watchElement.setAttribute("key", watchValue);
        watchElement.setAttribute("type", typeValue);

        element.setAttribute("xis:tag-handler", "watch");
        element.setAttribute("key", watchValue);

        this.domAccessor.surroundWith(element, watchElement);
        return element;
    }

    createTagHandler(watchElement) {
        return new WatchTagHandler(watchElement);
    }
}

tagRegistry.tagConfigs.push(new WatchTagConfig());