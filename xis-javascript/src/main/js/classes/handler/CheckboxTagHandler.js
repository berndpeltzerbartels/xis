class CheckboxTagHandler extends InputTagHandler {
    constructor(element) {
        super(element);
    }

    /**
     * Updates the checkbox state based on the form data.
     * @override
     */
    refreshFormData(data) {
        if (this.binding) {
            const path = doSplit(this.binding, '.');
            const value = data.getValue(path);
            if (Array.isArray(value)) {
                this.updateCheckedStateForArray(value);
            } else {
                this.updateCheckedStateForSingleValue(value);
            }
        }
    }

    /**
     * Returns the current value of the checkbox. In case no value-attribute is set explicitly,
     * we assume the value is "true".
     * 
     * @public
     * @override
     * @returns {any}
     */
    getValue() {
        if (this.tag.checked) {
            return this.tag.value || true; // Default to true if no value is set
        }
        return null;
    }

    /**
     * Updates the checked state of the checkbox based on an array of values.
     * 
     * @private
     * @param {Array<any>} valueArr 
     */
    updateCheckedStateForArray(valueArr) {
            this.tag.checked = valueArr.map(String).includes(String(this.tag.value));
    }

    /**
     * Updates the checked state of the checkbox based on a single value.
     * 
     * @param {any} value 
     */
    updateCheckedStateForSingleValue(value) {
        if (!isSet(this.tag.value) || this.tag.value === '') {
            this.tag.checked = value;
        } else {
            this.tag.checked = isSet(value) && String(value) === String(this.tag.value);
        }
    }
}