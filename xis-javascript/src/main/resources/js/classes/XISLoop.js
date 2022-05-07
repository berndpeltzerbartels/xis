class XISLoop extends XISValueHolder{

    /**
     * 
     * @param {XISTemplateObject} parent 
     * @param {XISLoopAttributes} loopAttributes 
     */
    constructor(parent, loopAttributes) {
        super(this, [
            loopAttributes.numberVarname,
            loopAttributes.indexVarName,
            loopAttributes.itemVarName
        ]);
        this.parent = parent;
        this.loopAttributes = loopAttributes;
        this.rows = [];
    }

    /**
     * @override
     */
    render() {
        var arr = this.getArray();
        this.updateRowCount(arr.length);
        for (var rowIndex = 0; rowIndex < arr.length; rowIndex++) {
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
}