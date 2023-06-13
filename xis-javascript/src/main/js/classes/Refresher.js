/**
 * Refreshes the document-tree after receiving data.
 */
class Refresher {


    refreshNode(node, data) {
        if (isElement(node, data)) {
            this.refreshElement(node, data);
        } else {
            this.refreshTextNode(node, data);
        }
    }

    /**
     * @public
     * @param {Element} element 
     * @param {Data} data 
     */
    refreshElement(element, data) {
        this.refreshAttributes(element, data);
        if (!this.invokeHandler(element, data)) {
            this.refreshChildNodes(element, data);
        }
    }

    /**
     * @private
     * @param {Element} element 
     * @param {Data} data 
     */
    refreshAttributes(element, data) {
        if (element._attributes) {
            for (var attribute of element._attributes) {
                element.setAttribute(attribute.name, attribute.expression.evaluate(data));
            }
        }
    }

    /**
     * 
     * @param {Element} element 
     * @param {Data} data 
     * @returns {boolean} true if the handler refreshes child-nodes
     */
    invokeHandler(element, data) {
        if (element._handler) {
            element._handler.refresh(data);
            return element._handler.type == 'foreach-handler' || element._handler.type == 'widget-container-handler';// these handlers are refreshing child nodes
        }
        return false;
    }

    /**
     * @public 
     * @param {Element} element 
     * @param {Data} data
     */
    refreshChildNodes(element, data) {
        for (var child of nodeListToArray(element.childNodes)) {
            this.refreshNode(child, data);
        }
    }

    /**
     * 
     * @param {Node} node 
     * @param {Data} data 
     */
    refreshTextNode(node, data) {
        if (node._expression) {
            var value = node._expression.evaluate(data);
            if (node.setNodeValue) {
                node.setNodeValue(value);
            } else {
                node.nodeValue = value;
            }

        }
    }
}

