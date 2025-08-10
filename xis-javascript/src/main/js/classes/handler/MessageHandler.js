class MessageHandler {

    constructor() {
        this.messageTag = document.getElementById('messages');
        this.errorMessages = [];
        this.infoMessages = [];
        this.warningMessages = [];
    }

    reportServerError(message) {
        this.addErrorMessage(message);
    }

    addValidationErrors(errors) {
        errors.forEach(error => this.addMessage(error, 'error', false));
        this._render(); // Einmal rendern nach dem Hinzufügen aller Fehler
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
        this._render();
    }

    addMessage(message, level, render = true) {
        const messageArray = this[`${level}Messages`];
        if (!messageArray.includes(message)) {
            messageArray.push(message);
            if (render) {
                this._render();
            }
        }
    }

    _render() {
        // Ersetzt: this.messageTag.innerHTML = '';
        while (this.messageTag.firstChild) {
            this.messageTag.removeChild(this.messageTag.firstChild);
        }

        this._createMessageBlockForLevel('error');
        this._createMessageBlockForLevel('warning');
        this._createMessageBlockForLevel('info');
    }

    _createMessageBlockForLevel(level) {
        const messages = this[`${level}Messages`];
        if (messages.length === 0) {
            return;
        }

        let container;
        if (messages.length === 1) {
            // Einzelne Nachricht in einem <div>
            container = document.createElement('div');
            container.className = level;
            container.appendChild(document.createTextNode(messages[0]));
        } else {
            // Mehrere Nachrichten in einer <ul>
            container = document.createElement('ul');
            container.className = level;
            messages.forEach(msg => {
                const li = document.createElement('li');
                li.textContent = msg;
                container.appendChild(li);
            });
        }
        this.messageTag.appendChild(container);
    }

    showToast(message, level = 'error') {
        const toast = document.createElement('div');
        toast.className = `toast align-items-center text-bg-${level} border-0 show position-fixed bottom-0 end-0 m-3`;
        toast.setAttribute('role', 'alert');

        // Ersetzt: toast.innerHTML = `...`
        const flexContainer = document.createElement('div');
        flexContainer.className = 'd-flex';

        const toastBody = document.createElement('div');
        toastBody.className = 'toast-body';
        toastBody.textContent = message;

        const closeButton = document.createElement('button');
        closeButton.type = 'button';
        closeButton.className = 'btn-close btn-close-white me-2 m-auto';
        closeButton.setAttribute('data-bs-dismiss', 'toast');

        flexContainer.appendChild(toastBody);
        flexContainer.appendChild(closeButton);
        toast.appendChild(flexContainer);

        document.body.appendChild(toast);
        setTimeout(() => toast.remove(), 5000);
    }
}