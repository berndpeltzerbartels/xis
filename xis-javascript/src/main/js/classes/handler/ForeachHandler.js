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
        return this.renderItemsRefresh(data);
    }

    /**
     * @private
     * @param {Data} data
     * @returns {Promise<void>}
     */
    renderItemsRefresh(data) {
        return this.doRenderItems(data, (handler, subData) => handler.refresh(subData));
    }

    /**
     * @private
     * Shared logic for rendering items, param handlerCall is a function (handler, subData) => Promise
     */
    doRenderItems(data, handlerCall) {
        debugger;
        const { arr, path } = this.getArrayData(data);
        this.cache.sizeUp(arr.length);
        const promises = [];
        for (let i = 0; i < this.cache.length; i++) {
            const subData = this.prepareSubData(data, i, arr[i]);
            const children = this.cache.getChildren(i);
            if (i < arr.length) {
                promises.push(...this.handleChildren(children, subData, handlerCall));
            } else {
                for (const child of children) {
                    if (child.parentNode === this.tag) this.tag.removeChild(child);
                    else break;
                }
            }
        }
        return Promise.all(promises).then(() => {});
    }

    /**
     * @private
     * Gets array and path from data
     */
    getArrayData(data) {
        const path = this.arrayPathExpression.evaluate(data);
        const arrayPath = this.doSplit(path, '.');
        const arr = data.getValue(arrayPath);
        if (!arr) throw new Error('No array with key "' + path + '" found for foreach');
        return { arr, path };
    }

    /**
     * @private
     * Prepares subData for each item
     */
    prepareSubData(data, i, value) {
        const subData = new Data({}, data);
        this.setValidationPath(subData, this.varName, i);
        subData.setValue([this.varName + 'Index'], i);
        subData.setValue([this.varName], value);
        return subData;
    }

    /**
     * @private
     * Handles children for each item, returns array of promises
     */
    handleChildren(children, subData, handlerCall) {
        const promises = [];
        for (const child of children) {
            if (child.parentNode !== this.tag) this.tag.appendChild(child);
            const handler = this.tagHandlers.getRootHandler(child);
            handler.parentHandler = this;
            this.descendantHandlers.push(handler);
            promises.push(handlerCall(handler, subData));
        }
        return promises;
    }


    setValidationPath(subData, varName, index) {
        if (!subData.validationPath) {
            return; // we are not inside a form
        }
        subData.validationPath += '/' + varName + '[' + index + ']';
    }
}