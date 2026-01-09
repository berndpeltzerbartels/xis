
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
    }

    addDescendantHandler(handler) {
        handler.parentHandler = this;
        this.descendantHandlers.push(handler);
        handler.publishBindEvent();
        return handler;
    }

    removeDescendantHandler(handler) {
        const handlers =[];
        for (var h of this.descendantHandlers) {
            if (h != handler) {
                handlers.push(h);
            }
        }
        this.descendantHandlers = handlers;
        handler.parentHandler = null;   
    }

    refresh(data) {
        throw new Error('abstract method: refresh must be implemented by subclass');
    }

    /**
     * Refreshes this handler and all descendants with state-aware data.
     * Prevents infinite recursion by skipping the original invoker.
     * 
     * @param {Data} data - Combined data including updated state values
     * @param {TagHandler} invoker - The handler that initiated the state change (will be skipped)
     */
    stateRefresh(data, invoker) {
        if (this === invoker) {
            // Skip the invoker to prevent infinite recursion
            return;
        }
        this.refresh(data); 
        for (var handler of this.descendantHandlers) {
            handler.stateRefresh(data, invoker);
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
     * @returns {Promise}
     */
    refreshDescendantHandlers(data) {
        var promises = [];
        for (var handler of this.descendantHandlers) {
            promises.push(handler.refresh(data));
        }
        return Promise.all(promises);
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
        return new TextContentParser(src, this).parse();
    }

    variableTextContentFromAttribute(attrName) {
        var attr = this.tag.getAttribute(attrName);
        if (attr) {
            return new TextContentParser(attr, this).parse();
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

    getParentFormHandler()  {
        var handler = this.findParentFormHandler();
        if (handler) {
            return handler;
        }
        throw new Error('no parent form-handler for ' + this.tag 
            +". May be you forgot to add xis:binding to the form?");
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
