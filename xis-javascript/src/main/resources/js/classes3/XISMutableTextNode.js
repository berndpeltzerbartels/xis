class XISMutableTextNode {


    refresh() {
        this.node.nodeValue = '123';
    }


    getText() {
        throw new Error('abstract method');
    }


}