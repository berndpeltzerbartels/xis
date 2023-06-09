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
    }




}