class XISBody extends XISTemplateObject {

    /**
     * 
     * @param {XISPage} parentPage 
     */
    constructor(parentPage) {
        super(parentPage);
        this.className = 'XISBody';
        this.childNodes = [];
    }


    appendChildNode(node) {
        this.childNodes.push(node);
    }

    removeChildNode(node) {
        //TODO  this.childNodes.push(node);
    }

    bind(rootPage) {
        this.childNodes.forEach(node => rootPage.bodyElement.appendChild(node));
    }

    unbind(rootPage) {
        this.childNodes.forEach(node => rootPage.bodyElement.appendChild(node));
    }
}