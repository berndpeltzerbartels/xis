class FormSubmitterHandler extends TagHandler {

    constructor(element) {
        super(element);
        this.actionExpression = this.variableTextContentFromAttribute('xis:action'); // mandatory
        element.addEventListener('click', event => {
            event.preventDefault();
            this.onClick(event);
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
     * @public
     * @returns {Promise}
     */
    reapply() {
        return this.refreshWithData(this.data). then(() => {
            return this.reapplyDescendantHandlers();
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

    onClick(event) {
        this.getParentFormHandler().submit(this.action);
    }

}