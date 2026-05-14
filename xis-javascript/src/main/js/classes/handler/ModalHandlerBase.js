class ModalHandlerBase extends TagHandler {

    constructor(element, type) {
        super(element);
        this.type = type;
        this.modalExpression = this.variableTextContentFromAttribute('xis:modal');
        this.parameters = {};

        element.addEventListener('click', event => {
            event.preventDefault();
            return Promise.resolve(this.openModal()).catch(error => handleError(error));
        });
    }

    refresh(data) {
        this.data = data;
        this.parameters = {};
        this.modal = this.modalExpression.evaluate(data);
        return this.refreshDescendantHandlers(data);
    }

    addParameter(name, value) {
        this.parameters[name] = value;
    }

    openModal() {
        return app.modals.open(this.modal, this.parameters, this.findParentFrontletContainerHandler());
    }
}
