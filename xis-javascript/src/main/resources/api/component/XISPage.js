
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
        this.head.init();
        this.body.init();
    }

    /**
     * @public
     */
    destroy() {
        this.head.destroy();
        this.body.destroy();
    }

    /**
     * @public
     */
    show() {
        this.head.show();
        this.body.show();
    }

    /**
     * @public
     */
    hide() {
        this.head.hide();
        this.body.hide();
    }

    /**
     * @public
     */
    getHead() {
        return this.rootPage.head;
    }

    /**
     * @public
     */
    getBody() {
        return this.rootPage.body;
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
                this.rootPage.title.innerText = child.innerText;
            } else {
                this.headChildNodes.push(this.rootPage.head.appendChild(child));
            }

        }
    }

    /**
    * @private 
    */
    bindBodyContent() {
        var nodeList = this.body.element.childNodes;
        for (var i = 0; i < nodeList.length; i++) {
            this.bodyChildNodes.push(this.rootPage.body.appendChild(nodeList.item(i)));
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
                this.rootPage.head.removeChild(child);
            }
        }
    }

    /**
     * @private
    */
    unbindBodyContent() {
        while (this.bodyChildNodes.length > 0) {
            var child = this.bodyChildNodes.pop();
            this.rootPage.body.removeChild(child);
        }
    }

    /**
     * Set html-attributes of this page's body to given body-tag.
     * @private
     */
    setBodyAttributes() {
        for (var name of this.body.element.getAttributeNames()) {
            var value = this.body.element.getAttribute(name);
            this.rootPage.body.setAttribute(name, value);
        }
    }

    /**
     * Remove html-attributes of this page's body from given body-tag.
     * @private
     */
    removeBodyAttributes() {
        for (var name of this.body.element.getAttributeNames()) {
            this.rootPage.body.removeAttribute(name);
        }
    }

    getContainer() {
        throw new Error('abstract method');
    }

    appendChild(childElement) {
        throw new Error('abstract method');
    }

    removeChild(childElement) {
        throw new Error('abstract method');
    }

    getElement() {
        throw new Error('abstract method');
    }

    unlink() {
        throw new Error('abstract method');
    }


}