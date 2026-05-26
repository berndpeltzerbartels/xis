class ModalManager {

    constructor(frontlets, initializer, tagHandlers) {
        this.frontlets = frontlets;
        this.initializer = initializer;
        this.tagHandlers = tagHandlers;
        this.current = undefined;
        this.counter = 0;
    }

    open(modal, parameters, parentContainerHandler) {
        const modalId = app.client.config.getFrontletId(modal);
        const modalParameters = mergeObjects(urlParameters(modal), parameters || {});
        if (!this.current) {
            this.current = this.createModal(parentContainerHandler);
        } else {
            this.current.parentContainerHandler = parentContainerHandler;
        }
        this.current.overlay.classList.add('xis-modal-open');
        this.current.overlay.removeAttribute('hidden');
        this.current.containerHandler.modalParameters = modalParameters;
        return this.current.containerHandler.showFrontlet(
            modalId,
            new FrontletState(app.pageController.resolvedURL, {}, modalParameters)
        );
    }

    handleActionResponse(response, parentContainerHandler) {
        if (response.actionProcessing === 'PAGE') {
            this.close();
            return app.pageController.handleActionResponse(response);
        }
        if (response.nextModalId) {
            const parent = this.current ? this.current.parentContainerHandler : parentContainerHandler;
            return this.openFromResponse(response, parent);
        }
        let promise = Promise.resolve();
        if (response.closeModal) {
            const parent = this.current ? this.current.parentContainerHandler : parentContainerHandler;
            this.close();
            updateStores(response);
            if (response.reloadModalParent) {
                promise = this.reloadParent(parent);
            }
        } else {
            if (this.current && response.modalParameters) {
                this.current.containerHandler.modalParameters = mergeObjects(this.current.containerHandler.modalParameters || {}, response.modalParameters);
            }
            updateStores(response);
        }
        return promise;
    }

    reloadParent(parentContainerHandler) {
        if (parentContainerHandler) {
            return parentContainerHandler.refresh(parentContainerHandler.data);
        }
        return app.pageController.handleUpdateEventNow();
    }

    openFromResponse(response, parentContainerHandler) {
        if (!this.current) {
            this.current = this.createModal(parentContainerHandler);
        } else {
            this.current.parentContainerHandler = parentContainerHandler;
        }
        this.current.overlay.classList.add('xis-modal-open');
        this.current.overlay.removeAttribute('hidden');
        this.current.containerHandler.modalParameters = response.modalParameters || {};
        response.nextFrontletId = response.nextModalId;
        return this.current.containerHandler.handleActionResponse(response);
    }

    close() {
        if (!this.current) {
            return;
        }
        if (this.current.overlay.parentNode) {
            this.current.overlay.parentNode.removeChild(this.current.overlay);
        }
        if (this.current.containerHandler && this.current.containerHandler.frontletInstance) {
            this.current.containerHandler.frontletInstance.dispose();
        }
        this.current = undefined;
    }

    createModal(parentContainerHandler) {
        const overlay = document.createElement('div');
        overlay.className = 'xis-modal-backdrop';
        overlay.setAttribute('hidden', 'hidden');

        const shell = document.createElement('div');
        shell.className = 'xis-modal-shell';
        shell.setAttribute('role', 'dialog');
        shell.setAttribute('aria-modal', 'true');

        const closeButton = document.createElement('button');
        closeButton.setAttribute('type', 'button');
        closeButton.className = 'xis-modal-close';
        closeButton.setAttribute('aria-label', 'Close');
        closeButton.innerHTML = '&times;';
        closeButton.addEventListener('click', event => {
            event.preventDefault();
            this.close();
        });

        const container = document.createElement('xis:frontlet-container');
        container.setAttribute('container-id', '__xis-modal-' + (++this.counter));
        container.className = 'xis-modal-content';

        shell.appendChild(closeButton);
        shell.appendChild(container);
        overlay.appendChild(shell);
        document.body.appendChild(overlay);

        this.initializer.initialize(overlay);
        const containerHandler = this.tagHandlers.getHandler(container);
        return {
            overlay: overlay,
            container: container,
            containerHandler: containerHandler,
            parentContainerHandler: parentContainerHandler
        };
    }
}
