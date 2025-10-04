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
     * @returns {Promise<void>}
     */
    refresh(data) {
        this.data = data;
        return this.renderItems(data);
    }

    reapply() {
        this.renderItems(this.data);
    }

    renderItems(data) {
        const path = this.arrayPathExpression.evaluate(data);
        const arrayPath = this.doSplit(path, '.');
        const arr = data.getValue(arrayPath);
        if (!arr) throw new Error('No array with key "' + path + '" found for foreach');
        this.cache.sizeUp(arr.length);
        const promises = [];
        for (let i = 0; i < this.cache.length; i++) {
            const subData = new Data({}, data);
            this.setValidationPath(subData, this.varName, i);
            subData.setValue([this.varName + 'Index'], i);
            subData.setValue([this.varName], arr[i]);
            const children = this.cache.getChildren(i);
            if (i < arr.length) {
                for (const child of children) {
                    if (child.parentNode !== this.tag) this.tag.appendChild(child);
                    const handler = this.tagHandlers.getRootHandler(child);
                    handler.parentHandler = this;
                    this.descendantHandlers.push(handler);
                    promises.push(handler.refresh(subData));
                }
            } else {
                for (const child of children) {
                    if (child.parentNode === this.tag) this.tag.removeChild(child);
                    else break;
                }
            }
        }
        return Promise.all(promises).then(() => {});
    }

    setValidationPath(subData, varName, index) {
        if (!subData.validationPath) {
            return; // we are not inside a form
        }
        subData.validationPath += '/' + varName + '[' + index + ']';
    }
}