
class FormHandler extends TagHandler {

    /**
     * 
     * @param {Element} formTag 
     * @param {Client} client 
     */
    constructor(formTag, client) {
        super(formTag)
        this.client = client;
        this.actionElements = {};
        this.formData = new Data({});
        this.pathExpr = new TextContentParser().parse(this.getAttribute('form-data'));
        var _this = this;
        formTag.addEventListener('submit', event => {
            event.preventDefault();
            _this.submit(event);
        });

    }

    submit(event) {

    }

    refresh(data) {
        var formDataPath = doSplit(this.pathExpr.evaluate(data));
        var path = doSplit(formDataPath, '.');
        this.actionElements = {};
        this.formData = new Data(data.getValue(path));
        this.refreshChildNodes(data);
    }


    registerFormElement(formElement) {
        this.formElements.push(formElement);
    }

    registerActionElement(name, element) {
        this.actionElements[name] = element;
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