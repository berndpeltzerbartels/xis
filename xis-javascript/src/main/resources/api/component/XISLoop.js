class XISLoop extends XISValueHolder {

    /**
     * @param {XISTemplateObject} parent 
     */
    constructor(parent) {
        super(parent);
        this.className = 'XISLoop';
        this.parent = parent;
        this.rows = [];
    }

    init() {
        // noop
    }

    /**
     * @override
     */
    render() {
        this.setVarnames([
            this.loopAttributes.numberVarname,
            this.loopAttributes.indexVarName,
            this.loopAttributes.itemVarName
        ]);
        var arr = this.getArray();
        this.updateRowCount(arr.length);
        for (var rowIndex = 0; rowIndex < arr.length; rowIndex++) {
            // TODO Eventuell ist es besser getValue zu modifizieren (this als 2 Parameter) so dass man hier erkennt wer etwas will
            // man findet dann den Child Index
            this.values[this.loopAttributes.itemVarName] = arr[rowIndex];
            this.values[this.loopAttributes.indexVarName] = rowIndex;
            this.values[this.loopAttributes.numberVarName] = rowIndex + 1;
            this.rows[rowIndex].render();
        }
    }

    updateRowCount(size) {
        while (this.rowCount() < size) {
            this.appendRow();
        }
        while (this.rowCount() > size) {
            this.removeRow();
        }
    }

    appendRow() {
        this.rows.push(this.createChildren());
    }

    removeRow() {
        var children = this.rows.pop();
        for (var i = 0; i < children.length; i++) {
            this.element.removeChild(children[i].element);
        }
    }


    /**
    * Creates the child-objects (not Dom-Elements)
    * 
    * @override
    * @returns {Array}
    */
    createChildren() {
        throw new Error('abstract method');
    }

    /**
     * @override
     * @returns {XISContainer}
     */
    getContainer() {
        return this.container;
    }
}