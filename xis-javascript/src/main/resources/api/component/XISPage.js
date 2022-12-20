
class XISPage extends XISComponent {

    constructor(client) {
        super(client);
        this.className = 'XISPage';
        this.rootPage = undefined;
        this.headChildNodes = [];
        this.bodyChildNodes = [];
        this.state = {};
        this.componentType = 'PAGE';
    }

    /**
     * @public
     * @param {XISRootPage} rootPage 
     */
    bind(rootPage) {
        this.rootPage = rootPage;
    }

    /**
     * @public
     * @param {String} uri 
     */
    replace(uri) {
        var page = pages.getPage(uri);
        this.rootPage.unbindPage();
        page.bind(this.rootPage);
    }

    /**
     * @public
     */
    init() {
        // TODO load data 
        this.head.init();
        this.body.init();
    }

    /**
     * @public
     */
    destroy() {
        // TODO load data 
        this.head.destroy();
        this.body.destroy();
    }

    /**
     * @public
     */
    show() {
        // TODO load data 
        this.head.show();
        this.body.show();
    }

    /**
     * @public
     */
    hide() {
        // TODO load data 
        this.head.hide();
        this.body.hide();
    }

    /**
     * @public
     */
    unbind() {
        this.unbindHeadContent();
        this.unbindBodyContent();
        this.removeBodyAttributes();
    }

    /**
     * @public
     */
    refresh() {
        this.unbindHeadContent();
        this.unbindBodyContent();
        this.removeBodyAttributes();

        this.head.refresh();
        this.body.refresh();

        this.setBodyAttributes();
        this.bindHeadContent();
        this.bindBodyContent();
    }

    appendChild(child) {
        // noop
    }


    getContainer() {
        throw new Error('unsupported opreation');
    }

    unlink() {
        throw new Error('unsupported operation');
    }

    getElement() {
        throw new Error('unsupported operation');
    }

    getParameterNames() {
        return []; // TODO
    }
    /**
     * @private
     */
    bindHeadContent() {
        var nodeList = this.head.element.childNodes;
        for (var i = 0; i < nodeList.length; i++) {
            var child = nodeList.item(i);
            if (child.localName == 'title') {
                this.rootPage.titleElement.innerText = child.innerText;
            } else {
                this.headChildNodes.push(this.rootPage.headElement.appendChild(child));
            }

        }
    }

    /**
    * @private 
    */
    bindBodyContent() {
        var nodeList = this.body.element.childNodes;
        for (var i = 0; i < nodeList.length; i++) {
            this.bodyChildNodes.push(this.rootPage.bodyElement.appendChild(nodeList.item(i)));
        }
    }

    /**
     * We do not remove all childnodes but only those ones from this page.
     * Otherwise we would remove all script-tags.
     * 
     * @private
     */
    unbindHeadContent() {
        while (this.headChildNodes.length > 0) {
            var child = this.headChildNodes.pop();
            if (child.localName != 'title') {
                this.rootPage.headElement.removeChild(child);
            }
        }
    }

    /**
     * @private
    */
    unbindBodyContent() {
        while (this.bodyChildNodes.length > 0) {
            var child = this.bodyChildNodes.pop();
            this.rootPage.bodyElement.removeChild(child);
        }
    }

    /**
     * Set html-attributes of this page's body to given body-tag.
     * @private
     */
    setBodyAttributes() {
        for (var name of this.body.element.getAttributeNames()) {
            var value = this.body.element.getAttribute(name);
            this.rootPage.bodyElement.setAttribute(name, value);
        }
    }

    /**
     * Remove html-attributes of this page's body from given body-tag.
     * @private
     */
    removeBodyAttributes() {
        for (var name of this.body.element.getAttributeNames()) {
            this.rootPage.bodyElement.removeAttribute(name);
        }
    }

    getContainer() {
        throw new Error('abstract method: getContainer()');
    }

    appendChild(childElement) {
        throw new Error('abstract method: appendChild(childElement');
    }

    removeChild(childElement) {
        throw new Error('abstract method: removeChild(childElement)');
    }

    getElement() {
        throw new Error('abstract method: getElement()');
    }

    unlink() {
        throw new Error('abstract method: unlink()');
    }


}