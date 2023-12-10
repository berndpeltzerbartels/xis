class FormElementHandler extends TagHandler {

    /**
     * 
     * @param {Element} element 
     */
    constructor(element) {
        super(element);
        this.value = element.value;
        this.binding = element.getAttribute('xis:binding');
        if (this.binding.indexOf('${')) throw new Error('binding must have no variables: ' + this.binding);
        this.init();
    }

    /**
     * @private
     */
    init() {
        var _this = this;
        this.updateState('pristine');
        element.addEventListener('change', event => {
            _this.updateState('edited');
            this.initiateValidation();
        });
        var form = this.findParentFormElement();
        if (!form) throw new Error('no parent form-tag or form-tag is not bound for ' + this.tag);
        this.formHandler = form._handler;
        this.formHandler.registerElementHandler(this);
    }


    refresh(data) {
        this.value = data.getValueByPath(this.binding);
        this.refreshDescendantHandlers(data);
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

