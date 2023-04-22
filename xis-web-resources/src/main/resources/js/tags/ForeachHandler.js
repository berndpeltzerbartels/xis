class ForeachHandler extends TagHandler {

    /**
     * @param {Element} tag the custom tag '<xis:foreach/>'
     */
    constructor(tag) {
        super(tag);
        this.arrayPath = this.doSplit(this.getAttribute('array'), '.');
        this.varName = this.getAttribute('var');
        this.cache = new NodeCache(this.childArray);
    }

    /**
     * @public
     * @param {Data} data
     */
    refresh(data) {
        var arr = data.getValue(this.arrayPath);
        this.cache.sizeUp(arr.length);
        for (var i = 0; i < this.cache.length; i++) {
            var subData = new Data({}, data);
            debug.debug(subData);
            subData.setValue(this.varName, arr[i]);
            var children = this.cache.getChildren(i);
            if (i < arr.length) {
                for (var child of children) {
                    if (child.parentNode != this.tag) {
                        debug.debug('append child in refresh of foreach-handler:', [this, child]);
                        console.log('foreach - appendChild');
                        this.tag.appendChild(child);
                    }
                    if (child.refresh) {
                        child.refresh(subData);
                    }
                }
            } else if (!child.parentNode) {
                break;
            }
        }
    }
}