
class TagHandler {

    /**
     * 
     * @param {Element} tag 
     */
    constructor(tag) {
        this.tag = tag;
        this.descendantHandlers = [];
        this.priority = 'normal';
    }

    addDescendantHandler(handler) {
        this.descendantHandlers.push(handler);
        handler.publishBindEvent();

    }

    removeDescendantHandler(handler) {
        this.descendantHandlers = this.descendantHandlers.filter(h => h != handler);
    }

    refresh(data) {
        throw new Error('abstract method');
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
     */
    refreshDescendantHandlers(data) {
        for (var handler of this.descendantHandlers) {
            handler.refresh(data);
        }
    }

    refreshFormData(data) {
        for (var handler of this.descendantHandlers) {
            handler.refreshFormData(data);
        }
    }

    refreshValidatorMessages(messages) {
        for (var handler of this.descendantHandlers) {
            handler.refreshValidatorMessages(messages);
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
        this.descendantHandlers = [];
    }

    findParentHtmlElement() {
        var element = this.tag;
        while (element) {
            debug.debug('findParentHtmlElement', element);
            if (this.isFrameworkElement(element)) {
                element = element.parentNode;
            } else {
                break;
            }
        }
        console.log('return findParentHtmlElement:' + element.localName);
        return element;
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
        return new TextContentParser(src).parse();
    }

    expressionFromAttribute(attrName) {
        var attr = this.tag.getAttribute(attrName);
        if (attr) {
            return new TextContentParser(attr).parse();
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

    /**
  * @protected
  * @returns {Element}
  */
    findParentFormElement() {
        var e = this.tag.parentNode;
        while (e) {
            if (this.isFrameworkFormElement(e)) {
                return e;
            }
            e = e.parentNode;
        }
    }

    /**
     * @private
     * @param {Element} element 
     * @returns 
     */
    isFrameworkFormElement(element) {
        return isElement(element) && element._handler && element._handler.type == 'form-handler';
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
