class HeadTagHandler extends TagHandler {

    /**
     * 
     * @param {TagHandlers} tagHandlers 
     */
    constructor(tagHandlers) {
        super(getElementByTagName('head'));
        this.tagHandlers = tagHandlers;
        this.title = getElementByTagName('title');
        this.type = 'head-handler';

    }

    /**
     * @public
     * @param {page} page
     */
    bind(page) {
        this.setTitleExpression(page);
        for (var node of page.headChildArray) {
            if (isElement(node) && node.localName == 'title') {
                continue;
            }
            this.tag.appendChild(node);
        }
        var headTemplateRootHandler = this.tagHandlers.getRootHandler(page.headTemplate);
        this.addDescendantHandler(headTemplateRootHandler);
    }

    /**
    * Removes all children from head-tag and put them bag to headTemplate, except title.
    *
    * @public
    * @param {Element} headTemplate
    */
    release(headTemplate) {
        for (var node of this.nodeListToArray(this.tag.childNodes)) {
            if (isElement(node) && node.getAttribute('ignore')) {
                continue;
            }
            this.tag.removeChild(node);
            headTemplate.appendChild(node);
        }
    }


    /**
     * @public
     * @override
     * @param {Data} data 
     * @returns {Promise}
     */
    refresh(data) {
        this.data = data;
        this.renderhWithData(data);
        return this.refreshDescendantHandlers(data);
    }
    /**
     * @private
     * @param {Data} data
     * @returns {Promise}
     */
    renderhWithData(data) {
        this.refreshTitle(data);
        return Promise.resolve();
    }

    /**
     * @param {Data} data 
     */
    refreshTitle(data) {
        if (!this.titleExpression) {
            return;
        }
        this.title.innerText = this.titleExpression.evaluate(data);
    }

    setTitle(title) {
        this.title.innerText = title;
    }

    setTitleExpression(page) {
        if (!page.titleElement) {
            this.titleExpression = undefined;
           return;
        }
        this.titleExpression = new TextContentParser(page.titleElement.innerText, () => this.refreshTitle(this.data)).parse();
    }

    clearTitle() {
        this.title.innerText = '';
    }

    /**
     * Commit any buffered head changes. Present for coordinator compatibility.
     */
    commitBuffer() {
        // Head currently updates live DOM (title.innerText and script src attributes).
        // No additional commit steps required, but method provided for symmetry.
    }


    /**
     * @param {Array} array
     * @param {Function} filterFunction
     * @returns {Array} An array of all elements matching the filterFunction
     * @description Removes all matching elements from the original array and returns them.
     */
    extractFromArrayInPlace(array, filterFunction) {
        const result = [];
        for (let i = array.length - 1; i >= 0; i--) {
            if (filterFunction(array[i])) {
                result.push(array[i]);
                array.splice(i, 1);
            }
        }
        return result.reverse(); // to preserve original order
    }
}