/**
 * A html-element with framework-attributes.
 */
class XISRepeatElement extends XISLoopElement {

    constructor(parent, loopAttributes) {
        super(parent, loopAttributes);
        this.elements = [];
        this.currentElement = undefined;
    }

    /**
     * @override
     */
    render() {
        if (this.evaluateIf()) {
            var arr = this.getArray();
            this.updateRowCount(arr.length);
            for (var rowIndex = 0; rowIndex < arr.length; rowIndex++) {
                this.values[this.loopAttributes.itemVarName] = arr[rowIndex];
                this.values[this.loopAttributes.itemVarName] = rowIndex;
                this.values[this.loopAttributes.numberVarName] = rowIndex + 1;
                this.currentElement = this.elements[rowIndex];
                this.updateAttribues(this.currentElement);
                this.rows[rowIndex].render();
            }
        } else {
            this.updateRowCount(0);
        }
    }

    rowCount() {
        return this.elements.length;
    }

    appendRow() {
       this.currentElement = this.createElement();
        this.parent.appendChild(this.currentElement);
        this.elements.push(this.currentElement);
        var children = this.createChildren();
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            this.rows.push(child);
            this.currentElement.appendChild(child.element);
        }
    }

    removeRow() {
        this.rows.pop();
        var element = this.elements.pop();
        this.parent.removeChild(element);
    }

    updateAttribues(element) {
        throw new Error('abstract method');
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
       return [];
    }

}