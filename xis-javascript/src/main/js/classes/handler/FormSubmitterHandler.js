class FormSubmitterHandler extends TagHandler {

    constructor(element) {
        super(element);
        this.actionExpression = this.expressionFromAttribute('xis:action'); // mandatory
        var form = this.findParentFormElement();
        this.formHandler = form._handler;
        if (!form) throw new Error('no parent form-tag or form-tag is not bound for ' + this.tag);
        element.addEventListener('click', event => {
            event.preventDefault();
            this.onClick(event);
        });
    }

    refresh(data) {
        this.action = this.actionExpression.evaluate(data);
    }

    onClick(event) {
        this.formHandler.submit(this.action);
    }

}