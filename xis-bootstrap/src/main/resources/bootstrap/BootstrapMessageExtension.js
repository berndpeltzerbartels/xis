class BootstrapMessageHandler {
    constructor() {
        this.messageTag = document.getElementById('messages');
        this.errorMessages = [];
        this.infoMessages = [];
        this.warningMessages = [];
        this.toastContainer = document.getElementById('toast');
    }

    reportServerError(message) {
        this.showToast(message, 'danger');
        this.addErrorMessage(message);
    }

    addValidationErrors(errors) {
        errors.forEach(error => this.addMessage(error, 'error', false));
        this._renderMessages(); // Einmal rendern nach dem Hinzufügen aller Fehler
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
        if (this.toastContainer) {
            // Ersetzt: this.toastContainer.innerHTML = '';
            while (this.toastContainer.firstChild) {
                this.toastContainer.removeChild(this.toastContainer.firstChild);
            }
        }
        this._renderMessages();
    }

    addMessage(message, level, render = true) {
        const messageArray = this[`${level}Messages`];
        if (!messageArray.includes(message)) {
            messageArray.push(message);
            if (render) {
                this._renderMessages();
            }
        }
    }

    _renderMessages() {
        // Ersetzt: this.messageTag.innerHTML = '';
        while (this.messageTag.firstChild) {
            this.messageTag.removeChild(this.messageTag.firstChild);
        }

        this._createAlertForLevel('error', 'alert-danger');
        this._createAlertForLevel('warning', 'alert-warning');
        this._createAlertForLevel('info', 'alert-info');
    }

    _createAlertForLevel(level, alertClass) {
        const messages = this[`${level}Messages`];
        if (messages.length === 0) {
            return;
        }

        const div = document.createElement('div');
        div.className = `alert ${alertClass} alert-dismissible fade show`;
        div.setAttribute('role', 'alert');

        if (messages.length === 1) {
            // Einzelne Nachricht ohne Liste
            div.appendChild(document.createTextNode(messages[0]));
        } else {
            // Mehrere Nachrichten als Liste
            const ul = document.createElement('ul');
            ul.className = 'mb-0';
            messages.forEach(msg => {
                const li = document.createElement('li');
                li.textContent = msg;
                ul.appendChild(li);
            });
            div.appendChild(ul);
        }

        // Schließen-Button hinzufügen
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'btn-close';
        button.setAttribute('data-bs-dismiss', 'alert');
        button.setAttribute('aria-label', 'Close');
        div.appendChild(button);

        this.messageTag.appendChild(div);
    }

    showToast(message, level = 'danger') {
        if (!this.toastContainer) return;
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

        this.toastContainer.appendChild(toast);
        setTimeout(() => {
            toast.remove();
        }, 4000);
    }
}

function initializeBootstrap(app) {
   app.messageHandler = new BootstrapMessageHandler();
}

window.app.initializers.push(initializeBootstrap);