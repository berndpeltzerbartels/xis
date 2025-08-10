class BootstrapMessageHandler {
    constructor() {
        this.messageTag = document.getElementById('messages');
        this.parentTag = this.messageTag.parentNode;
        this.errorMessages = [];
        this.infoMessages = [];
        this.warningMessages = [];
        this.toastContainer = document.getElementById('toast'); // Vorausgesetzt, das div#toast existiert
    }

    reportServerError(message) {
        this.showToast(message, 'danger');
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
        if (this.toastContainer) {
            this.toastContainer.innerHTML = '';
        }
    }

    addMessage(message, level) {
        // Bootstrap Alert-Klassen zuweisen
        let alertClass;
        switch (level) {
            case 'error':
                alertClass = 'alert-danger';
                break;
            case 'warning':
                alertClass = 'alert-warning';
                break;
            case 'info':
            default:
                alertClass = 'alert-info';
        }

        const messageArray = this[`${level}Messages`];
        if (messageArray.includes(message)) {
            return; // message already exists, do not add again
        }
        messageArray.push(message);

        // Bootstrap Alert-Element erzeugen
        const div = document.createElement('div');
        div.className = `alert ${alertClass} alert-dismissible fade show`;
        div.setAttribute('role', 'alert');
        div.textContent = message;

        // Optional: Close-Button hinzufügen
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
        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">${message}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;
        this.toastContainer.appendChild(toast);
        setTimeout(() => {
            toast.remove();
        }, 4000);
    }
}