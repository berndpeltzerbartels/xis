class FormSubmitterHandler extends TagHandler {

    constructor(element) {
        super(element);
        this.actionExpression = this.variableTextContentFromAttribute('xis:action'); // mandatory
        this.actionParameters = {};
        element.addEventListener('click', event => {
            event.preventDefault();
            return Promise.resolve(this.onClick(event)).catch(error => handleError(error));
        });
    }


    /**
     * @public
     * @param {Data} data
     * @returns {Promise}
     */
    refresh(data) {
        this.data = data;
        return this.refreshWithData(data). then(() => {
            return this.refreshDescendantHandlers(data);
        });
    }

    /**
     * @private
     * @param {Data} data
     * @returns {Promise}
     */
    refreshWithData(data) {
        this.action = this.actionExpression.evaluate(data);
        return Promise.resolve();
    }

    addParameter(name, value) {
        this.actionParameters[name] = value;
    }

    onClick(event) {
        return this.getParentFormHandler().submit(this.action, this.actionParameters);
    }

}
