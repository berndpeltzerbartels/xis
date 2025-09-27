class ForeachHandler extends TagHandler {

    /**
     * @param {Element} tag the custom tag '<xis:foreach/>'
     * @param {TagHandler} tagHandlers
     */
    constructor(tag, tagHandlers) {
        super(tag);
        this.tagHandlers = tagHandlers;
        this.arrayPathExpression = this.variableTextContentFromAttribute('array');
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
        this.data = data;
        this.renderItems(data);
    }

    reapply() {
        this.renderItems(this.data);
    }

    renderItems(data) {
        var arrayPath = this.doSplit(this.arrayPathExpression.evaluate(data), '.');
        var arr = data.getValue(arrayPath);
        if (!arr) {
          return;
        }
        this.cache.sizeUp(arr.length);
        for (var i = 0; i < this.cache.length; i++) {
            var subData = new Data({}, data);
            this.setValidationPath(subData, this.varName, i);
            subData.setValue([this.varName + 'Index'], i);
            subData.setValue([this.varName], arr[i]);
            var children = this.cache.getChildren(i);
            if (i < arr.length) {
                for (var child of children) {
                    if (child.parentNode != this.tag) {
                        this.tag.appendChild(child);
                    }
                    const childHandler = this.tagHandlers.getRootHandler(child);
                    childHandler.parentHandler = this;
                    this.descendantHandlers.push(childHandler);
                    childHandler.refresh(subData);
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