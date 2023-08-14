class ParameterHandler extends TagHandler {

    /**
     * @param {Element} element 
     * @param {Parameter} parameter 
     */
    constructor(element, parameter) {
        super(element);
        this.parameter = parameter;
    }

    refresh(data) {
        this.parameter.refresh(data);
        var acceptor = this.findParentParameterAcceptor();
        if (!acceptor) {
            throw new Error('no parent tag found for parameter');
        }
        acceptor.addParameter(this.parameter.name, this.parameter.value);
    }

    findParentParameterAcceptor() {
        var e = element;
        while (e) {
            if (e._handler && e._handler.addParameter) {
                return e;
            }
            e = e.parentNode;
        }
    }


}