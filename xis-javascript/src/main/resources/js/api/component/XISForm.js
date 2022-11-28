class XISForm {

    /**
     * @param {XISComponent} component 
     */
    constructor(component) {
        this.component = component;
        this.className = 'XISForm';
        this.lockingElements = new XISSet();
    }

    lock(formElement) {
        this.lockingElements.add(formElement);
    }

    unlock(formElement) {
        this.lockingElements.remove(formElement);
    }

    submit(action) {
        if (this.lockingElements.isEmpty()) {
            this.component.onAction(action);
        }
    }
}