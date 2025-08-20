package test.page.forms.textarea;

import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;

@Page("/textarea.html")
@RequiredArgsConstructor
class TextareaPage {

    private final TextareaFormService service;

    @FormData("formData")
    public TextareaFormModel formData() {
        return service.getTextareaFormModel();
    }

    @Action
    public void submit(@FormData("formData") TextareaFormModel formData) {
        service.saveTextareaFormModel(formData);
    }
}