
/**
 * Caching childnodes to avoid unnecessary expensive
 * cloning and initializing of elements and text nodes.
 *
 * @property {array<Node>} nodeArray
 * @property {array<array<Node>>} cache
 */
class ForEachNodeCache {

    /**
     * @param {array<Node>} nodeArray
     */
    constructor(nodeArray) {
        this.nodeArray = nodeArray;
        this.cache = [nodeArray];
    }

    /**
     * @public
     * @param {Number} size
     */
    sizeUp(size) {
        while (this.cache.length < size) {
            this.cache.push(this.cloneChildNodes());
        }
    }

    /**
    * @public
    * @param {Number} length
    */
    get length() {
        return this.cache.length;
    }

    /**
     *
     * @param {Number} index
     * @returns {array<Node>}
     */
    getChildren(index) {
        return this.cache[index];
    }

    /**
     * @param {Node} node
     * @returns {array<Node>}
     */
    cloneChildNodes() {
        var clones = [];
        for (var node of this.nodeArray) {
            var clone = this.cloneNode(node);
            app.initializer.initialize(clone);
            clones.push(clone);
        }
        return clones;
    }

    cloneNode(node) {
        if (isElement(node)) {
            return this.cloneElement(node);
        } else {
            return this.cloneTextNode(node);
        }
    }

    /**
     * 
     * @param {Element} element 
     */
    cloneElement(element) {
        var clone = document.createElement(element.localName);
        for (var name of element.getAttributeNames()) {
            clone.setAttribute(name, element.getAttribute(name));
        }
        for (let index = 0; index < element.childNodes.length; index++) {
            const child = element.childNodes.item(index);
            clone.appendChild(this.cloneNode(child));
        }
        return clone;
    }

    cloneTextNode(node) {
        return document.createTextNode(node.nodeValue);
    }
}

