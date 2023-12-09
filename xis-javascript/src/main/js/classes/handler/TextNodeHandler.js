class TextNodeHandler extends TagHandler { // TODO Basisklasse mit Refresh-Methode für Taghandler erstellen, hier verwenden

    constructor(node) {
        super(node);
        this.node = node;
        this.expression = new TextContentParser(node.nodeValue).parse();
    }

    /**
     * @public
     * @override
     * @param {Data} data 
     */
    refresh(data) {
        this.node.nodeValue = this.expression.evaluate(data);
        nodeValueChanged(this.node);
    }
}