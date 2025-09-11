class MessageHandler {
    /**
     * @param {HTMLElement} [container=document.getElementById('messages')]
     * @param {Object} [options]
     * @param {{info:number|null, warning:number|null, error:number|null}} [options.autoHideDurations]
     *        ms per level; null disables auto-hide for that level.
     */
    constructor(container, options = {}) {
        this.messageTag = container || document.getElementById('messages');
        if (!this.messageTag) {
            console.warn('[MessageHandler] No container element found (id="messages"). Rendering is disabled.');
        }

        this.errorMessages = [];
        this.infoMessages = [];
        this.warningMessages = [];

        // Single timer per level
        this._timers = { error: null, warning: null, info: null };

        const defaults = { info: 5000, warning: 8000, error: null };
        this._autoHideDurations = Object.assign({}, defaults, options.autoHideDurations);
    }

    reportServerError(message) {
        this.addErrorMessage(message);
    }

    /**
     * Validation messages: no auto-hide timer here.
     * Accepts: string[], or { field: string[] }.
     */
    addValidationErrors(errors) {
    debugger;
        if (Array.isArray(errors)) {
            errors.forEach(msg => this.addMessage(msg, 'error', { render: false, autoHide: false }));
        } else if (errors && typeof errors === 'object') {
            Object.values(errors).forEach(arr => {
                if (Array.isArray(arr)) {
                    arr.forEach(msg => this.addMessage(msg, 'error', { render: false, autoHide: false }));
                }
            });
        } else if (errors != null) {
            this.addMessage(String(errors), 'error', { render: false, autoHide: false });
        }
        this._render();
    }

    addInfoMessage(message)    { this.addMessage(message, 'info'); }
    addWarningMessage(message) { this.addMessage(message, 'warning'); }
    addErrorMessage(message)   { this.addMessage(message, 'error'); }

    /** Clears all levels and cancels timers. */
    clearMessages() {
        this.errorMessages = [];
        this.infoMessages = [];
        this.warningMessages = [];
        this._cancelTimer('error');
        this._cancelTimer('warning');
        this._cancelTimer('info');
        this._render();
    }

    clearErrors()   { this._clearLevel('error'); }
    clearWarnings() { this._clearLevel('warning'); }
    clearInfos()    { this._clearLevel('info'); }

    /**
     * Adds a message at a given level.
     * 3rd param is backward-compatible:
     *  - boolean => render immediately (autoHide = default by level)
     *  - object  => { render?: boolean, autoHide?: boolean }
     */
    addMessage(message, level, renderOrOptions = true) {
        const { render, autoHide } = this._normalizeAddOptions(level, renderOrOptions);

        const msg = this._coerceMessage(message);
        const bucketKey = `${level}Messages`;
        const messageArray = this[bucketKey];

        if (!Array.isArray(messageArray)) {
            console.warn(`[MessageHandler] Unknown level: ${level}`);
            return;
        }

        if (!messageArray.includes(msg)) {
            messageArray.push(msg);

            // Start per-level timer if configured and not already running.
            if (autoHide) this._ensureTimer(level);

            if (render) this._render();
        }
    }

    /* --------------------------- Internal helpers --------------------------- */

    _normalizeAddOptions(level, renderOrOptions) {
        if (typeof renderOrOptions === 'boolean') {
            return { render: renderOrOptions, autoHide: this._autoHideDurations[level] != null };
        }
        const render = renderOrOptions?.render !== undefined ? !!renderOrOptions.render : true;
        const autoHide = renderOrOptions?.autoHide !== undefined
            ? !!renderOrOptions.autoHide
            : (this._autoHideDurations[level] != null);
        return { render, autoHide };
    }

    _coerceMessage(input) {
        if (input == null) return '';
        if (typeof input === 'string') return input;
        if (input instanceof Error) return input.message || String(input);
        return String(input);
    }

    _clearLevel(level) {
        this[`${level}Messages`] = [];
        this._cancelTimer(level);
        this._render();
    }

    _ensureTimer(level) {
        const duration = this._autoHideDurations[level];
        if (duration == null) return; // auto-hide disabled for this level
        if (this._timers[level] != null) return; // keep existing timer; do not reset

        this._timers[level] = setTimeout(() => {
            this._timers[level] = null;
            this[`${level}Messages`] = [];
            this._render();
        }, duration);
    }

    _cancelTimer(level) {
        const id = this._timers[level];
        if (id != null) {
            clearTimeout(id);
            this._timers[level] = null;
        }
    }

    _render() {
        if (!this.messageTag) return;

        // Clear container (no innerHTML)
        while (this.messageTag.firstChild) {
            this.messageTag.removeChild(this.messageTag.firstChild);
        }

        // Build and append blocks directly (no DocumentFragment for GraalVM compatibility)
        const errorEl   = this._createLevelBlock('error');
        const warningEl = this._createLevelBlock('warning');
        const infoEl    = this._createLevelBlock('info');

        if (errorEl)   this.messageTag.appendChild(errorEl);
        if (warningEl) this.messageTag.appendChild(warningEl);
        if (infoEl)    this.messageTag.appendChild(infoEl);
    }

    _createLevelBlock(level) {
        const messages = this[`${level}Messages`];
        if (!messages || messages.length === 0) return null;

        let container;
        const isSingle = messages.length === 1;

        if (isSingle) {
            container = document.createElement('div');
            container.className = level;
            container.appendChild(document.createTextNode(messages[0]));
        } else {
            container = document.createElement('ul');
            container.className = level;
            messages.forEach(msg => {
                const li = document.createElement('li');
                li.textContent = msg;
                container.appendChild(li);
            });
        }

        // Accessibility roles without styling implications
        if (level === 'error') {
            container.setAttribute('role', 'alert');
            container.setAttribute('aria-live', 'assertive');
        } else {
            container.setAttribute('role', 'status');
            container.setAttribute('aria-live', 'polite');
        }

        return container;
    }
}
