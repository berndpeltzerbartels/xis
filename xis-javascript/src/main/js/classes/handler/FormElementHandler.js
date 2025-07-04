class FormElementHandler extends TagHandler {

    /**
     * 
     * @param {Element} element 
     */
    constructor(element) {
        super(element);
        this.bindingExpression = new TextContentParser(element.getAttribute('xis:binding'), this).parse();
        this.binding = undefined;
    }


    refresh(data) {
        this.binding = this.bindingExpression.evaluate(data);
        this.refreshDescendantHandlers(data);
    }

    refreshFormData(data) {
        if (this.binding) {
            var path = doSplit(this.binding, '.');
            var value = data.getValue(path);
            if (value === undefined || value === null) {
                value = '';
            }
            this.tag.value = value;
            const formHandler = this.getParentFormHandler();
           
            formHandler.onElementHandlerRefreshed(this, this.binding);
        }
        super.refreshFormData(data);
    }


    getValue() {
        return this.tag.value;
    }

    /**
     * @protected
     * @override
     */
    onBind() {
        this.valid = true;
    }


    /**
     * @protected
     * @param {string} state 
     */
    updateState(state) {
        this.state = state;
        if (!this.tag.classList.contains(state)) {
            this.tag.classList.add(state);
        }

    }
}

