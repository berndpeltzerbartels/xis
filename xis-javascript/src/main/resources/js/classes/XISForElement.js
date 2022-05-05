class XISForElement {

    constructor(parent, repeatAttributes) {
        super(parent, repeatAttributes);
        this.element = this.createElement();
    }

    
    /**
     * @override
     */
     render() {
        if (this.evaluateIf()) {
            var arr = this.getArray();
            this.updateRowCount(arr.length);
            for (var rowIndex = 0;rowIndex < arr.length; rowIndex++) {
                this.values[this.loopAttributes.itemVarName] = arr[rowIndex];
                this.values[this.loopAttributes.itemVarName] = rowIndex;
                this.values[this.loopAttributes.numberVarName] = rowIndex + 1;
                this.rows[rowIndex].render();
            }
        } else {
            this.updateRowCount(0);
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


    updateAttribues() {
    
    }

    evaluateIf() {
        return true;
    }

    /**
     * Creates the DOM-Element.
     * 
     * @override
     * @returns {any}
     */
    createElement() {
        throw new Error('abstract method');
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