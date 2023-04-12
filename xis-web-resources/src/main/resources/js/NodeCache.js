
/**
 * Caching childnodes to avoid unnecessary expensive
 * cloning and initializing of elements and text nodes.
 *
 * @property {array<Node>} nodeArray
 * @property {array<array<Node>>} cache
 */
class NodeCache {

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
    cloneChildNodes(node) {
        var clones = [];
        for (node of this.nodeArray) {
            clones.push(node.cloneNode);
        }
        return clones;
    }
}

