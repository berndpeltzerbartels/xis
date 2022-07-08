class XISLoop extends XISValueHolder{

    /**
     * @param {XISTemplateObject} parent 
     */
    constructor(parent) {
        super(parent.getValueHolder());
        this.container = parent.getContainer();
        this.parent = parent;
        this.loopAttributes = this.getLoopAttributes();
        this.setVarnames( [
            this.loopAttributes.numberVarname,
            this.loopAttributes.indexVarName,
            this.loopAttributes.itemVarName
        ]);
        this.rows = [];
    }


    getLoopAttributes() {
        throw new Error('abstract method');
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

    /**
     * @override
     * @returns {XISValueHolder}
     */
     getValueHolder() {
        return this;
     }
 
     /**
      * @override
      * @returns {XISContainer}
      */
     getContainer() {
         return this.container;
     }
}