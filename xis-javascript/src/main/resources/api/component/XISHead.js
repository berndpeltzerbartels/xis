class XISHead extends XISTemplateObject {

    constructor(parentPage) {
        super(parentPage);
        this.className = 'XISHead';
        this.childNodes = [];
    }


    appendChildNode(node) {
        this.childNodes.push(node);
    }

    removeChildNode(node) {
        //TODO  this.childNodes.push(node);
    }

    bindRoot(rootPage) {
        this.childNodes.forEach(node => rootPage.headElement.appendChild(node));
    }

    unbindRoot(rootPage) {
        this.childNodes.forEach(node => rootPage.headElement.appendChild(node));
    }


}