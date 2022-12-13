/**
 * Content of an element (html-tag) containing variables like
 *
 * <div>My name is ${name}</div>
 *
 */
class XISMutableTextNode extends XISTemplateObject {

    constructor(parent) {
        super(parent);
        this.className = 'XISMutableTextNode';
        this.node = this.createNode();
    }

    init() {
        this.parent.getElement().appendChild(this.node);
    }

    refresh() {
        debugger;
        this.node.nodeValue = this.getText();
    }


    getText() {
        throw new Error('abstract method');
    }


    createNode() {
        throw new Error('abstract method');
    }

}