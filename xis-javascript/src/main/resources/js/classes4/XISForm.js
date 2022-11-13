class XISForm {

    constructor() {
        this.className = 'XISForm';
        this.lockingElements = new XISArrayList();
    }

    lock(formElement) {
        this.lockingElements.add(formElement);
    }

    unlock(formElement) {
        this.lockingElements.remove(formElement);
    }

    submit() {
        if (this.lockingElements.isEmpty()) {

        }
    }
}