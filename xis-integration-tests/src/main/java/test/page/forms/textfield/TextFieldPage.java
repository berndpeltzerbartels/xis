package test.page.forms.textfield;

import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;

@Page("/textField.html")
@RequiredArgsConstructor
class TextFieldPage {

    private final TextFieldFormService service;

    @FormData("formData")
    public TextFieldFormModel formData() {
        return service.getTextFieldFormModel();
    }

    @Action
    public void submit(@FormData("formData") TextFieldFormModel formData) {
        service.saveTextFieldFormModel(formData);
    }
}