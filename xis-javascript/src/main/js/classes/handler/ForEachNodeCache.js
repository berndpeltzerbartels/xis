
/**
 * Caching childnodes to avoid unnecessary expensive
 * cloning and initializing of elements and text nodes.
 *
 * @property {array<Node>} nodeArray
 * @property {array<array<Node>>} cache
 * @property {ForeachHandler} cache
 */
class ForEachNodeCache {

    /**
     * @param {array<Node>} nodeArray
     */
    constructor(nodeArray) {
        this.nodeArray = nodeArray;
        this.nodeCache = [nodeArray];
        for (var node of nodeArray) {
            this.initialize(node);
        }
    }

    /**
     * @public
     * @param {Number} size
     */
    sizeUp(size) {
        while (this.nodeCache.length < size) {
            this.nodeCache.push(this.cloneChildNodes());
        }
    }

    /**
    * @public
    * @param {Number} length
    */
    get length() {
        return this.nodeCache.length;
    }

    /**
     * @public
     * @param {Number} index
     * @returns {array<Node>}
     */
    getChildren(index) {
        return this.nodeCache[index];
    }

    /**
     * @private
     * @param {Node} node
     * @returns {array<Node>}
     */
    cloneChildNodes() {
        var clones = [];
        for (var node of this.nodeArray) {
            var clone = this.cloneNode(node);
            this.initialize(clone);
            clones.push(clone);
        }
        return clones;
    }

    /**
     * @private
     * @param {Node} node 
     * @returns {Node} 
     */
    cloneNode(node) {
        if (isElement(node)) {
            return this.cloneElement(node);
        } else {
            return this.cloneTextNode(node);
        }
    }

    /**
     * @private
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

    /**
     * @private
     * @param {Node} node 
     * @returns 
     */
    cloneTextNode(node) {
        return document.createTextNode(node.nodeValue);
    }

    /**
     * @private
     * @param {Node} node 
     */
    initialize(node) {
        app.initializer.initialize(node, null)
    }
}

