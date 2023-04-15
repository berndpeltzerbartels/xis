class TagHandler {

    constructor(tag) {
        this.tag = tag;
        this.childArray = this.nodeListToArray(tag.childNodes);
    }

    refresh(data) {
        throw new Error('abstract method');
    }

    clearChildren() {
        for (node of this.childArray) {
            if (node.parentNode) {
                node.parentNode.removeChild(node);
            }
        }
    }

    findParentHtmlElement() {
        var element = this;
        while (element) {
            if (this.isFrameworkElement(element)) {
                element = element.parentNode;
            } else {
                break;
            }
        }
        return element;
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
