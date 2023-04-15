class ForeachHandler extends TagHandler {

    /**
     * @param {Element} tag the custom tag '<xis:foreach/>'
     */
    constructor(tag) {
        super(tag);
        this.arrayPath = this.doSplit(this.getAttribute('array'), '.');
        this.varName = this.getAttribute('var');
        this.cache = new NodeCache(this.childArray);
        this.parent = this.findParentHtmlElement();
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
            subData.setValue(this.varName, arr[i]);
            var children = this.cache.getChildren(i);
            if (i < arr.length) {
                for (child of children) {
                    if (child.parentNode != this.parent) {
                        this.parent.appendChild(child);
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