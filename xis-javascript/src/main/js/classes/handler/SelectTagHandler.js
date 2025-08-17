class SelectTagHandler extends InputTagHandler {
    constructor(element) {
        super(element);
    }


    /**
     * @override
     */
    refreshFormData(data) {
        if (this.binding) {
            var path = doSplit(this.binding, '.');
            var value = data.getValue(path);
            this.updateOptions(value);
        }
    }

    /**
     * Returns the current value of the select element. In case the select allows selecting multiple options, 
     * this will return an array of selected values.
     * 
     * @public
     * @override
     * @returns {any}
     */
    getValue() {
        debugger;
        const isMultiple = this.tag.multiple;
        if (isMultiple) {
            return Array.from(this.tag.selectedOptions).map(option => option.value);
        }
        return this.tag.value;
    }

    /**
     * @private
     * @param {any} value 
     */
    updateOptions(value) {
        const isArray = Array.isArray(value);
        for (const option of this.optionElements()) {
            if (isArray) {
                option.selected = value.includes(option.value);
            } else if (value === null) {
                option.selected = (option.value === '' || option.value === null);
            } else {
                option.selected = this.isEqual(option.value, value);
            }
        }
    }

    /**
     * @private
     * @returns {NodeList}
     */
    optionElements() {
        var selectTag = this.tag;
        var options = [];
        for (var i = 0; i < selectTag.options.length; i++) {
            options.push(selectTag.options.item(i));
        }
        return options;
    }

    /**
 * Compares two values for equality, treating numbers and strings with the same content as equal.
 * @private
 * @param {any} v1
 * @param {any} v2
 * @returns {boolean}
 */
isEqual(v1, v2) {
    // Treat null and undefined as not equal to anything except themselves
    if (!isSet(v1)) {
        return !isSet(v2);
    }
    // Compare as strings
    return String(v1) === String(v2);
}
}