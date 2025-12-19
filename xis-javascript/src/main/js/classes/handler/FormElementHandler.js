class FormElementHandler extends TagHandler {

    /**
     * 
     * @param {Element} element 
     */
    constructor(element) {
        super(element);
        this.bindingExpression = new TextContentParser(element.getAttribute('xis:binding')).parse();
        this.errorClassExpression = new TextContentParser(element.getAttribute('xis:error-class')).parse();;
        this.binding = undefined;
        element.addEventListener('change', () => {
            const formHandler = this.getParentFormHandler();
            formHandler.onFormValueChanges(this);
            if (this.lastErrorClass) {
                this.removeCssClass(this.lastErrorClass);
                this.lastErrorClass = null;
            }
        });
    }


    /**
     * @public
     * @param {Data} data
     * @returns {Promise}
     */
    refresh(data) {
        this.data = data;
        this.refreshWithData(data);
        this.refreshErrorBinding(data);
        return this.refreshDescendantHandlers(data);
    }

    /**
     * @private
     * @param {Data} data
     */
    refreshWithData(data) {
        this.binding = this.bindingExpression.evaluate(data);
        this.errorBinding = data.validationPath + '/' + this.binding;
        const formHandler = this.getParentFormHandler();
        formHandler.onElementHandlerRefreshed(this, this.binding);
    }

    refreshFormData(data) {
        if (this.binding) {
            var path = doSplit(this.binding, '.');
            var value = data.getValue(path);
            if (value === undefined || value === null) {
                value = '';
            }
            this.tag.value = value;
        }
        super.refreshFormData(data);
    }

    refreshErrorBinding(data) {
        const errorClass = this.errorClassExpression.evaluate(data);
        if (this.lastErrorClass) {
            this.removeCssClass(this.lastErrorClass);
            this.lastErrorClass = null;
        }
        if (errorClass) {
            this.errorClass = errorClass;
            const formHandler = this.getParentFormHandler();
            formHandler.onMessageHandlerRefreshed(this, this.errorBinding);
        }
    }

    refreshValidatorMessages(validatorMessages) {
        if (this.errorClass) {
            const errorMessage = validatorMessages.messages[this.errorBinding];
            if (errorMessage) {
                this.lastErrorClass = this.errorClass;
                this.addCssClass(this.errorClass);
            } else if (this.lastErrorClass) {
                this.removeCssClass(this.lastErrorClass);
                this.lastErrorClass = null;
            }
        }
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
     * @param {string} cssClass 
     */
    addCssClass(cssClass) {
        if (!this.tag.classList.contains(cssClass)) {
            this.tag.classList.add(cssClass);
        }
    }

    /**
     * @protected
     * @param {string} cssClass 
     */
    removeCssClass(cssClass) {
        if (this.tag.classList.contains(cssClass)) {
            this.tag.classList.remove(cssClass);
        }
    }


    reset() {
    }
}

