class MessageHandler {

    constructor() {
        this.messageTag = document.getElementById('messages');
        this.parentTag = this.messageTag.parentNode;
        this.errorMessages = [];
        this.infoMessages = [];
        this.warningMessages = [];
    }

    reportServerError(message) {
        this.addErrorMessage(message);
    }

    addValidationErrors(errors) {
        errors.forEach(error => this.addErrorMessage(error));
    }

    addInfosMessage(message) {
        this.addMessage(message, 'info');
    }

    addWarningMessage(message) {
        this.addMessage(message, 'warning');
    }

    addErrorMessage(message) {
        this.addMessage(message, 'error');
    }

    clearMessages() {
        this.errorMessages = [];
        this.infoMessages = [];
        this.warningMessages = [];
        this.messageTag.innerHTML = '';
    }

    addMessage(message, level) {
        // ul with matching class/level
        const ul = this.messageTag.querySelector(`ul.${level}`);
        if (!ul) {
            // messageTag with level for class
            this.messageTag.innerHTML = `<ul class="${level}"></ul>`;
        }
        const messageArray = this[`${level}Messages`];
        if (messageArray.includes(message)) {
            return; // message already exists, do not add again
        }
        messageArray.push(message);
        // Create a new list item and append it to the ul
        const li = document.createElement('li');
        li.textContent = message;
        li.classList.add(level);
        this.messageTag.querySelector('ul').appendChild(li);
    }

    showToast(message, level = 'error') {
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-bg-${level} border-0 show position-fixed bottom-0 end-0 m-3`;
    toast.setAttribute('role', 'alert');
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 5000);
}


}
