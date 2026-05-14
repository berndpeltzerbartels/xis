package test.page.modal;

import one.xis.FormData;
import one.xis.Modal;
import one.xis.Parameter;

@Modal("/modal-integration/form-only")
class FormOnlyModal {

    @FormData("edit")
    EditModal.EditForm edit(@Parameter("source") String source) {
        return new EditModal.EditForm(source);
    }
}
