
class TagHandler {

    /**
     * 
     * @param {Element} tag 
     */
    constructor(tag) {
        this.tag = tag;
        this.descendantHandlers = [];
        this.parentHandler = null;
        this.type = 'tag-handler';
        this.priority = 'normal';
        this.expressionParser = new ExpressionParser(elFunctions);
        this.reactiveVariables = new Set();
        this.hasGlobals = false;
    }

    /**
     * Default reapply implementation: reapply on descendants.
     * Handlers may override to implement custom behavior.
     * @param {TagHandler} invoker optional - the handler that initiated the reapply
     * @returns {Promise<void>}
     */
    reapply(invoker) {
        return this.reapplyDescendantHandlers(invoker);
    }

    /**
     * Re-applies (evaluates) descendant handlers. The invoker is forwarded to children.
     * Note: many handlers use this.data internally; callers may pass an invoker or nothing.
     * @param {TagHandler} invoker
     * @returns {Promise<void>}
     */
    reapplyDescendantHandlers(invoker) {
        const promises = [];
        for (const handler of this.descendantHandlers) {
            if (typeof handler.reapply === 'function') {
                try {
                    promises.push(handler.reapply(invoker));
                } catch (e) {
                    // ensure a promise is present even if handler.reapply throws synchronously
                    promises.push(Promise.reject(e));
                }
            } else {
                promises.push(Promise.resolve());
            }
        }
        return Promise.all(promises).then(() => {});
    }


    registerReactiveListener(context, path, listener) {
       this.appendAttribute.eventPublisher.addEventListener(context, path, listener);
    }

    addDescendantHandler(handler) {
        handler.parentHandler = this;
        this.descendantHandlers.push(handler);
        handler.publishBindEvent();
        return handler;
    }

    removeDescendantHandler(handler) {
        const handlers = [];
        for (var h of this.descendantHandlers) {
            if (h != handler) {
                handlers.push(h);
            }
        }
        this.descendantHandlers = handlers;
        handler.parentHandler = null;
    }

    refresh(data) {
        throw new Error('abstract method');
    }


    refreshWithStoredData(invoker) {
        if (invoker == this) {
            return;
        }
        for (var handler of this.descendantHandlers) {
            handler.refreshWithStoredData(invoker);
        }
    }

    notifyGlobals() {
        if (this.parentHandler) {
            this.parentHandler.notifyGlobals();
        }
    }

    /**
     * @protected
     */
    publishBindEvent() {
        this.onBind();
        for (var handler of this.descendantHandlers) {
            handler.publishBindEvent();
        }
    }

    /**
     * @protected
     */
    onBind() {

    }

    /**
     * Refreshes descendant handlers.
     * 
     * @param {Data} data 
     * @returns {Promise<void>}
     */
    refreshDescendantHandlers(data) {
        const promises = [];
        for (const handler of this.descendantHandlers) {
            promises.push(handler.refresh(data));
        }
        return Promise.all(promises).then(() => {});
    }


    refreshFormData(data) {
        for (var handler of this.descendantHandlers) {
            handler.refreshFormData(data);
        }
    }

    clearChildren() {
        for (var node of this.nodeListToArray(this.tag.childNodes)) {
            if (node.getAttribute && node.getAttribute('ignore')) {
                continue;
            }
            if (node.parentNode) {
                node.parentNode.removeChild(node);
            }
        }
        for (var handler of this.descendantHandlers) {
            handler.parentHandler = null;
        }
        this.descendantHandlers = [];
    }

    appendAttribute(attrName, appendValue) {
        if (this.tag.getAttribute('xis:submit-onkeyup')) {
            var attr = '';
            if (element.getAttribute(attrName)) {
                attr += element.getAttribute(attrName);
                if (!trim(attr).endsWith(";")) {
                    attr += ';'
                }
            }
            attr += appendValue;
            this.tag.setAttribute('onkeyup', attr);
        }
    }

    createExpression(src) {
        const listener = (context, path) => {
            const key = `${context}.${path}`;
            if (!this.reactiveVariables.has(key)) {
                this.reactiveVariables.add(key);
                app.eventPublisher.addEventListener(EventType.REACTIVE_DATA_CHANGED, () => {
                    this.reapply();
                });
            }
        };
        return new TextContentParser(src, listener).parse();
    }

    variableTextContentFromAttribute(attrName) {
        var attr = this.tag.getAttribute(attrName);
        if (attr) {
            const listener = (context, path) => {
                const key = `${context}.${path}`;
                if (!this.reactiveVariables.has(key)) {
                    this.reactiveVariables.add(key);
                    app.eventPublisher.addEventListener(EventType.REACTIVE_DATA_CHANGED, () => {
                        this.reapply();
                    });
                }
            };
            return new TextContentParser(attr, listener).parse();
        }
    }

    expressionFromAttribute(attrName) {
        var attr = this.tag.getAttribute(attrName);
        if (attr) {
            return this.expressionParser.parse(attr);
        }
    }

    findParentWidgetContainer() {
        var e = this.tag;
        while (e) {
            if (e.localName == 'xis:widget-container') {
                return e;
            }
            e = e.parentNode;
        }
    }

    getParentFormHandler() {
        var handler = this.findParentFormHandler();
        if (handler) {
            return handler;
        }
        throw new Error('no parent form-handler for ' + this.tag
            + ". May be you forgot to add xis:binding to the form?");
    }

    findParentFormHandler() {
        var handler = this;
        while (handler) {
            if (handler.type == 'form-handler') {
                return handler;
            }
            handler = handler.parentHandler;
        }
    }

    findParentWidgetContainerHandler() {
        var handler = this;
        while (handler) {
            if (handler.type == 'widget-container-handler') {
                return handler;
            }
            handler = handler.parentHandler;
        }
        return null;
    }

    isFrameworkElement(node) {
        return isElement(node) && node.localName.startsWith('xis:');
    }


    isVisible(node) {
        return node.parentNode != null;
    }

    nodeListToArray(nodeList) {
        var arr = [];
        for (var i = 0; i < nodeList.length; i++) {
            arr.push(nodeList.item(i));
        }
        return arr;
    }

    getAttribute(name) {
        return this.tag.getAttribute(name);
    }

    hasAttribute(name) {
        return this.tag.hasAttribute(name);
    }

    doSplit(string, separatorChar) {
        var rv = [];
        var buffer = '';
        for (var i = 0; i < string.length; i++) {
            var c = string.charAt(i);
            if (c === separatorChar) {
                rv.push(buffer);
                buffer = '';
            } else {
                buffer += c;
            }
        }
        rv.push(buffer);
        return rv;
    }
}
