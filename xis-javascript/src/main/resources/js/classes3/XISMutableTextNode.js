class XISMutableTextNode {

    init() {
        // noop
    }

    refresh() {
        this.node.nodeValue = '123';
    }


    getText() {
        throw new Error('abstract method');
    }


}