class FormElementHandler extends TagHandler {

    /**
     * 
     * @param {Element} element 
     */
    constructor(element) {
        super(element);
        this.bindingExpression = new TextContentParser(element.getAttribute('xis:binding')).parse();
        this.binding = undefined;
        this.init();
    }

    /**
     * @private
     */
    init() {
        var form = this.findParentFormElement();
        if (!form) throw new Error('no parent form-tag or form-tag is not bound for ' + this.tag);
        this.formHandler = form.handler;
    }



    refresh(data) {
        this.binding = this.bindingExpression.evaluate(data);
        this.refreshDescendantHandlers(data);
    }

    refreshFormData(data) {
        if (this.binding) {
            var path = doSplit(this.binding, '.');
            this.tag.value = data.getValue(path);
            this.formHandler.onElementHandlerRefreshed(this, this.binding);
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

