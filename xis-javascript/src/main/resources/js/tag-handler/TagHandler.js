class TagHandler {

    constructor(tag) {
        this.tag = tag;
        this.childArray = this.nodeListToArray(tag.childNodes);
        this.priority = 'normal';
    }

    refresh(data) {
        throw new Error('abstract method');
    }


    refreshChildNodes(data) {
        refresher.refreshChildNodes(this.tag, data);
    }

    clearChildren() {
        for (node of this.childArray) {
            if (node.parentNode) {
                node.parentNode.removeChild(node);
            }
        }
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

    currentPageId() {
        return pageController.pageId;
    }

    currentWidgetId() {
        var container = this.findParentWidgetContainer();
        return container ? container.widgetId : undefined;
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
