package test.page.modal;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Parameter;
import one.xis.ModelData;
import one.xis.Modal;
import one.xis.ModalResponse;
import one.xis.validation.Mandatory;

@Modal("/modal-integration/edit")
class EditModal {

    private final ModalIntegrationService service;

    EditModal(ModalIntegrationService service) {
        this.service = service;
    }

    @ModelData("modalSource")
    String modalSource(@Parameter("source") String source) {
        service.modalLoaded(source);
        return source;
    }

    @FormData("edit")
    EditForm edit(@Parameter("source") String source) {
        return new EditForm(source);
    }

    @Action
    ModalResponse save(@FormData("edit") EditForm form) {
        service.save(form.value());
        return ModalResponse.close().reloadParent();
    }

    record EditForm(@Mandatory String value) {
    }
}
