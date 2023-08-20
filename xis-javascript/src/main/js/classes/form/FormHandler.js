
class FormHandler extends TagHandler {

    constructor(form) {
        super(tag)
        this.formElements = [];
        var sendAttributeValue = this.getAttribute('xis:send');

    }

    refresh(data) {
        this.formElements = [];
        this.refreshChildNodes(data);
    }


    registerFormElement(formElement) {
        this.formElements.push();

    }

    validateSendAttribute(value) {
        switch (value) {
            case 'never':
            case 'onsubmit':
            case 'onkey':
            case 'onkeyup':

        }
    }


}