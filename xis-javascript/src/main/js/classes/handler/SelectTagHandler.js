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
            const formHandler = this.getParentFormHandler();
            formHandler.onElementHandlerRefreshed(this, this.binding);
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
                option.selected = (option.value === value);
            }
        }
    }

    /**
     * @private
     * @returns {NodeList}
     */
    optionElements() {
        var selectTag = this.tag;
        return Array.prototype.filter.call(
            selectTag.childNodes,
            node => node.nodeType === 1 && node.tagName === 'OPTION'
        );
    }
}