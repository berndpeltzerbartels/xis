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
        this.cache = new ForEachNodeCache(nodeListToArray(this.tag.childNodes));
        this.clearChildren();

    }

    /**
     * @public
     * @param {Data} data
     */
    refresh(data) {
        var arrayPath = this.doSplit(this.arrayPathExpression.evaluate(data), '.');
        var arr = data.getValue(arrayPath);
        this.cache.sizeUp(arr.length);
        for (var i = 0; i < this.cache.length; i++) {
            var subData = new Data({}, data);
            this.setValidationPath(subData, this.varName, i);
            subData.setValue([this.varName + '_index'], i);
            subData.setValue([this.varName], arr[i]);
            var children = this.cache.getChildren(i);
            if (i < arr.length) {
                for (var child of children) {
                    if (child.parentNode != this.tag) {
                        this.tag.appendChild(child);
                    }
                    child._rootHandler.refresh(subData);
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

    setValidationPath(subData, varName, index) {
        if (!subData.validationPath) {
            return; // we are not inside a form
        }
        subData.validationPath += '/' + varName + '[' + index + ']';
    }
}