package test.page.forms.checkbox;

import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;

@Page("/checkBox.html")
@RequiredArgsConstructor
class CheckBoxPage {

    private final CheckBoxFormService checkBoxFormService;

    @FormData("formData")
    public CheckBoxFormModel formData() {
        return checkBoxFormService.getCheckBoxFormModel();
    }

    @Action
    public void submit(@FormData("formData") CheckBoxFormModel formData) {
        checkBoxFormService.saveCheckBoxFormModel(formData);
    }
}