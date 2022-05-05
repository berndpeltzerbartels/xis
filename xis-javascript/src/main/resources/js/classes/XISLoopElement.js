class XISLoopElement {

    constructor(parent, repeatAttributes) {
        this.parent = parent;
        this.repeatAttributes = repeatAttributes;
        this.varNames = [
            repeatAttributes.numberVarname,
            repeatAttributes.indexVarName,
            repeatAttributes.itemVarName
        ];
        this.rows = [];
        this.values = {};
    }

    /**
     * @override
     */
    render() {
        throw new Error('abstract method');
    }

    appendChild(child) {
        throw new Error('abstract method');
    }

    updateRowCount(size) {
        while (this.rowCount() < size) {
            this.appendRow();
        }
        while (this.rowCount() > size) {
            this.removeRow();
        }
    }

    rowCount() {
        throw new Error('abstract method');
    }

    appendRow() {
        throw new Error('abstract method');
    }

    removeRow() {
        throw new Error('abstract method');
    }

    getValue(path) {
        var name = path[0];
        if (this.varNames.indexOf(name) != -1) {
            var rv = this.values[name];
            for (var i = 1; i < path.length; i++) {
                if (!rv) {
                    return undefined;
                }
                rv = rv[path[i]];
            }
            return rv;
        }
        return this.valueHolder.getValue(path);
    }
    getArray() {
        return this.valueHolder.getValue(this.repeatAttributes.arrayPath);
    }

    evaluateIf() {
        return true;
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