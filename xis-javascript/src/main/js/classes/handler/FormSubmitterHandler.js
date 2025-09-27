class FormSubmitterHandler extends TagHandler {

    constructor(element) {
        super(element);
        this.actionExpression = this.variableTextContentFromAttribute('xis:action'); // mandatory
        element.addEventListener('click', event => {
            event.preventDefault();
            this.onClick(event);
        });
    }

    refresh(data) {
        this.data = data;
        this.action = this.actionExpression.evaluate(data);
    }

    onClick(event) {
        this.getParentFormHandler().submit(this.action);
    }

}