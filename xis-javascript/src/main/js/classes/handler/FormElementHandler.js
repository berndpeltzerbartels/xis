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
        var _this = this;
        this.updateState('pristine');
        this.tag.addEventListener('change', event => {
            _this.updateState('edited');
            this.initiateValidation();
        });
        var form = this.findParentFormElement();
        if (!form) throw new Error('no parent form-tag or form-tag is not bound for ' + this.tag);
        this.formHandler = form._handler;
    }


    refresh(data) {
        this.binding = this.bindingExpression.evaluate(data);
        this.tag.value = data.getValueByPath(this.binding);
        this.formHandler.registerElementHandler(this);
        this.refreshDescendantHandlers(data);

    }


    getValue() {
        return this.tag.value;
    }

    /**
     * @protected
     * @override
     */
    onBind() {
        this.updateState('pristine');
    }

    /**
     * @protected
     */
    initiateValidation() {
        this.formHandler.validate();
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

