class ErrorStyleHandler extends TagHandler {

    constructor(tag) {
        super(tag);
        this.type = 'error-style-handler';
        this.errorStyleClnassExpression = new TextContentParser(this.tag.getAttribute('xis:error-class'), this).parse();
        this.bindingExpression = new TextContentParser(this.tag.getAttribute('xis:binding'), this).parse();
        this.lastErrorStyle = null;
    }

    refresh(data) {
        if (this.lastErrorStyle) {
            this.tag.classList.remove(this.lastErrorStyle);
            this.lastErrorStyle = null;
        }
        var errorStyleClass = this.errorStyleClassExpression.evaluate(data);
        if (!errorStyleClass) {
            return;
        }
        var errorMessagePath = ['validation', 'errors', this.bindingExpression.evaluate(data)];
        var errorMessage = data.getValue(errorMessagePath);
        if (!errorMessage) {
            return; // no error message, nothing to do
        }
        this.lastErrorStyle = errorStyleClass;
        this.tag.classList.add(errorStyleClass);
    }
}