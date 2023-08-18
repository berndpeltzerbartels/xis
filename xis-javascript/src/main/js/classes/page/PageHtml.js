
class PageHtml {

    constructor() {
        this.head = getElementByTagName('head');
        this.body = getElementByTagName('body');
        this.title = getElementByTagName('title');
        this.titleExpression = undefined;
    }


    /**
    * @public
    * @param {Page} page 
    */
    bindPage(page) {
        this.titleExpression = page.titleExpression;
        this.clearChildren(this.head);
        this.clearChildren(this.body);
        this.clearBodyAttributes();
        this.bindHeadChildNodes(page.headChildArray);
        this.bindBodyAttributes(page.bodyAttributes);
        this.bindBodyChildNodes(page.bodyChildArray);
    }


    /**
     * @public
     * @param {Response} data
     * @param {ResolvedURL} resolvedURL
     * @returns 
     */
    refresh(data, resolvedURL) {
        if (this.titleExpression) {
            getElementByTagName('title').innerText = this.titleExpression.evaluate(data);
        }
        refreshNode(getElementByTagName('head'), data);
        refreshNode(getElementByTagName('body'), data);
        this.updateHistory(resolvedURL);
    }

    /**
     * @public
     */
    reset() {
        this.clearBodyAttributes();
        this.clearChildren(this.head);
        this.clearChildren(this.body);
        this.title.innerText = undefined;
        this.titleExpression = undefined;
    }



    /**
    * @private
    * @param {Array<Node>} attributes 
    */
    bindHeadChildNodes(nodeArray) {
        for (var node of nodeArray) {
            if (node.nodeType == 1 && node.localName == 'title') {
                continue;
            }
            this.head.appendChild(node);
        }
    }

    /**
     * @private
     * @param {Array<Node>} attributes 
     */
    bindBodyChildNodes(nodeArray) {
        for (var node of nodeArray) {
            this.body.appendChild(node);
        }
    }

    /**
     * @private
     * @param {any} attributes 
     */
    bindBodyAttributes(attributes) {
        for (var name of Object.keys(attributes)) {
            this.body.setAttribute(name, attributes[name]);
        }
        app.initializer.initializeAttributes(this.body);
    }

    /**
     * Removes all attributes from body-tag except onload.
     *
     * @private
     */
    clearBodyAttributes() {
        for (var name of this.body.getAttributeNames()) {
            if (name == 'unload') {
                continue;
            }
            this.body.removeAttribute(name);
        }
        this.body._attributes = undefined;
    }



    /**
     * Updates value displayed in browser's address-input.
     * @param {ResolvedURL} resolvedURL
     * @private
     */
    updateHistory(resolvedURL) {
        var title = getElementByTagName('title').innerText;
        window.history.replaceState({}, title, resolvedURL.url);
    }


    /**
    * Removes child nodes of the element.
    * 
    * @private
    * @param {Element} element 
    */
    clearChildren(element) {
        for (var node of nodeListToArray(element.childNodes)) {
            if (node.getAttribute && node.getAttribute('ignore')) {
                continue;
            }
            element.removeChild(node);
        }
    }

}