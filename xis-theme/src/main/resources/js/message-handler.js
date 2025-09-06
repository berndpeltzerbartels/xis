class XisThemeMessageHandler {
    constructor() {
        this.validatorMessages = [];
    }

    reportServerError(message) {
        this.showToast(message, 'error');
    }

    addValidationErrors(errors) {
        this.validatorMessages = errors;
        const validatorMessageContainer = document.getElementById('messages');
        if (!validatorMessageContainer) return;
        validatorMessageContainer.innerHTML = '';
        errors.forEach(error => {
            const div = document.createElement('div');
            div.className = 'form-error-global';
            div.textContent = error;
            validatorMessageContainer.appendChild(div);
        });
    }

    addInfosMessage(message) {
        this.showToast(message, 'info');
    }

    addInfoMessage(message) {
        this.showToast(message, 'info');
    }

    addWarningMessage(message) {
        this.showToast(message, 'warning');
    }

    addErrorMessage(message) {
       this.showToast(message, 'error');
    }

    clearMessages() {
        this.validatorMessages = [];
         const toastContainer = document.getElementById('toast-container');
         if (toastContainer)
            toastContainer.innerHTML = '';
        const validatorMessageContainer = document.getElementById('messages');
        if (validatorMessageContainer)
            validatorMessageContainer.innerHTML = '';
    }


    showToast(message, level = 'error') {
        const toastContainer = document.getElementById('toast-container');
        if (!toastContainer) return;
        const toastDiv = document.createElement('div');
        toastDiv.classList.add('toast');
        toastDiv.classList.add(level);
        toastContainer.appendChild(toastDiv);
        toastDiv.innerHTML = message;
        setTimeout(() => toastDiv.remove(), 5000);
    }
}


eventListenerRegistry.addEventListener(EventType.APP_INITIALIZED , app => {
    app.messageHandler = new XisThemeMessageHandler();
});