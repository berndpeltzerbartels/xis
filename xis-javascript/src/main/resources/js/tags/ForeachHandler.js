class ForeachHandler extends TagHandler {

    /**
     * @param {Element} tag the custom tag '<xis:foreach/>'
     * @param {Initializer} initializer
     */
    constructor(tag, initializer) {
        super(tag);
        this.initializer = initializer;
        this.arrayPath = this.doSplit(this.getAttribute('array'), '.');
        this.varName = this.getAttribute('var');
        this.type = 'foreach-handler';
        this.priority = 'high';
    }

    /**
     * @public
     * @param {Data} data
     */
    refresh(data) {
        var arr = data.getValue(this.arrayPath);
        this.nodeCache().sizeUp(arr.length);
        for (var i = 0; i < this.cache.length; i++) {
            var subData = new Data({}, data);
            subData.setValue(this.varName, arr[i]);
            var children = this.nodeCache().getChildren(i);
            if (i < arr.length) {
                for (var child of children) {
                    if (child.parentNode != this.tag) {
                        this.tag.appendChild(child);
                    }
                    refreshNode(child, subData);
                }
            } else {
                // Cache is too long. We remove unused elements 
                for (var child of children) {
                    if (child.parentNode == this.tag) {
                        this.tag.removeChild(child);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    nodeCache() {
        if (!this.cache) {
            this.cache = new NodeCache(nodeListToArray(this.tag.childNodes), this.initializer);
        }
        return this.cache;
    }
}