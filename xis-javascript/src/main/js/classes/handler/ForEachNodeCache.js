
/**
 * Caching child nodes to avoid unnecessary expensive
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
        this.templateNodeArray = nodeArray;
        this.nodeCache = [];
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
        for (var node of this.templateNodeArray) {
            var clone = cloneNode(node);
            this.initialize(clone);
            clones.push(clone);
        }
        return clones;
    }

    /**
     * @private
     * @param {Node} node 
     */
    initialize(node) {
        app.initializer.initialize(node, null)
    }
}

