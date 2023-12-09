class FormElementHandler extends TagHandler {

    constructor(element) {
        super(element);
        this.bindingExpr = new TextContentParser(element.getAttribute('xis:binding')).parse();
        if (this.getAttribute('xis:submit-onkeyup')) {
            this.appendAttribute('onkeyup', 'this._handler.submit();');
        }
        if (this.getAttribute('xis:submit-onchange')) {
            this.appendAttribute('onchange', 'this._handler.submit();');
        }
    }

    refresh(data) {
        var binding = this.bindingExpr.evaluate(data);
        var formElement = this.parentFormElement();
        var formHandler = formElement._handler;
        var value = formHandler.formData.getValue(binding);
        this.tag.value = value;
    }


    parentFormElement() {
        var e = this.tag.parentNode;
        while (e) {
            if (e.localName == 'form') {
                return e;
            }
            e = e.parentNode;
        }
        throw new Error('no parent form-tag for ' + this.tag);

    }




    submit() {

    }



}

