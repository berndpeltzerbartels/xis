package test.page.modal;

import one.xis.Action;
import one.xis.Parameter;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.ModalResponse;
import one.xis.Page;

@Page("/modal-integration.html")
class ModalIntegrationPage {

    private final ModalIntegrationService service;

    ModalIntegrationPage(ModalIntegrationService service) {
        this.service = service;
    }

    @ModelData
    int pageVersion() {
        return service.pageVersion();
    }

    @ModelData
    String savedValue() {
        return service.savedValue();
    }

    @FormData("parent")
    ParentForm parentForm() {
        return new ParentForm(service.savedValue());
    }

    @Action
    ModalResponse openFromAction() {
        return ModalResponse.open(EditModal.class).parameter("source", "action");
    }

    @Action
    ModalResponse openFromParameter(@Parameter("source") String source) {
        return ModalResponse.open(EditModal.class).parameter("source", source);
    }

    @Action
    ModalResponse openFormOnlyModalFromParameter(@Parameter("source") String source) {
        return ModalResponse.open(FormOnlyModal.class).parameter("source", source);
    }

    record ParentForm(String value) {
    }
}
