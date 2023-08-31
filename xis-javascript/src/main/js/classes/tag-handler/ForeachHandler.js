class ForeachHandler extends TagHandler {

    /**
     * @param {Element} tag the custom tag '<xis:foreach/>'
     */
    constructor(tag) {
        super(tag);
        this.arrayPathExpression = this.createExpression(this.getAttribute('array'), '.');
        this.varName = this.getAttribute('var');
        this.type = 'foreach-handler';
        this.priority = 'high';
    }

    /**
     * @public
     * @param {Data} data
     */
    refresh(data) {
        var arrayPath = this.doSplit(this.arrayPathExpression.evaluate(data), '.');
        var arr = data.getValue(arrayPath);
        this.nodeCache().sizeUp(arr.length);
        for (var i = 0; i < this.cache.length; i++) {
            var subData = new Data({}, data);
            subData.setValue([this.varName + '-index'], i);
            subData.setValue([this.varName], arr[i]);
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
            this.cache = new NodeCache(nodeListToArray(this.tag.childNodes));
        }
        return this.cache;
    }
}