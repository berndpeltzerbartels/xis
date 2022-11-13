class XISFormElement {

    /**
     * 
     * @param {XISForm} form 
     */
    constructor(form) {
        this.className = 'XISFormElement';
        this.form = form;
    }

    onchange() {
        this.form.lock(this);
        this.validate();
        this.form.unlock(this);
    }


    validate() {

    }

}