package test.page.forms;

import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.Page;

import java.util.List;

@Page("/selectBoxForeachTagTemplateForm.html")
@RequiredArgsConstructor
class SelectBoxForeachTagTemplateFormPage {

    private final SelectBoxFormService selectBoxFormService;

    @ModelData
    public List<SelectBoxFormOption> options() {
        return selectBoxFormService.options();
    }

    @FormData("formData")
    public SelectBoxFormModel formData() {
        return selectBoxFormService.getSelectBoxFormModel();
    }

    @Action
    public void submit(@FormData("formData") SelectBoxFormModel formData) {
        selectBoxFormService.saveSelectBoxFormModel(formData);
    }
}
