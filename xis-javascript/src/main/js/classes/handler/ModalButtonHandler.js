class ModalButtonHandler extends ModalHandlerBase {

    constructor(element) {
        super(element, 'modal-button-handler');

        if (element.tagName.toLowerCase() === 'button' && !element.getAttribute('type')) {
            element.setAttribute('type', 'button');
        }
    }
}
