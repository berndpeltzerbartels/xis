class ErrorStyleHandler extends TagHandler {

    constructor(tag) {
        super(tag);
        this.type = 'error-style-handler';
        this.errorStyleClassExpression = new TextContentParser(this.tag.getAttribute('xis:error-class'), () => this.reapply()).parse();
        this.bindingExpression = new TextContentParser(this.tag.getAttribute('xis:binding'), () => this.reapply()).parse();
        this.lastErrorStyle = null;
    }

    refresh(data) {
        this.data = data;
        if (this.lastErrorStyle) {
            this.tag.classList.remove(this.lastErrorStyle);
            this.lastErrorStyle = null;
        }
        var errorStyleClass = this.errorStyleClassExpression.evaluate(data);
        if (!errorStyleClass) {
            return Promise.resolve();
        }
        var errorMessagePath = ['validation', 'errors', this.bindingExpression.evaluate(data)];
        var errorMessage = data.getValue(errorMessagePath);
        if (!errorMessage) {
            return Promise.resolve(); // no error message, nothing to do
        }
        this.lastErrorStyle = errorStyleClass;
        this.tag.classList.add(errorStyleClass);
        return Promise.resolve();
    }

    reapply(invoker) {
       //noop
    }
}