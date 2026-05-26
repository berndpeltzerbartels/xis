package test.page.modal;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Modal;
import one.xis.ModalResponse;
import one.xis.ModalParameter;
import one.xis.validation.Mandatory;

@Modal("/modal-integration/card/{card}/edit")
class EditCardModal {

    private final ModalIntegrationService service;

    EditCardModal(ModalIntegrationService service) {
        this.service = service;
    }

    @FormData("cardForm")
    CardForm cardForm() {
        return new CardForm("");
    }

    @Action
    ModalResponse save(@ModalParameter("card") String card, @FormData("cardForm") CardForm form) {
        service.saveCardValue(card, form.value());
        return ModalResponse.close().reloadParent();
    }

    record CardForm(@Mandatory String value) {
    }
}
