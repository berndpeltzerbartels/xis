class Refresher {


    refreshNode(node, data) {
        if (isElement(node, data)) {
            this.refreshElement(node, data);
        } else {
            this.refreshTextNode(node, data);
        }
    }

    /**
     * 
     * @param {Element} element 
     * @param {Data} data 
     */
    refreshElement(element, data) {
        if (element._attributes) {
            for (var attribute of element._attributes) {
                element.setAttribute(attribute.name, attribute.expression.evaluate(data));
            }
        }
        if (element._handler) {
            element._handler.refresh(data); // invokes child modes, too
        } else {
            for (var i = 0; i < element.childNodes.length; i++) {
                var child = element.childNodes.item(i);
                this.refreshNode(child, data);
            }
        }
    }

    refreshTextNode(node, data) {
        if (node._expression) {
            node.nodeValue = node._expression.evaluate(data);
        }
    }
}

var refresher = new Refresher();

function refreshNode(node, data) {
    refresher.refreshNode(node, data);
}