package test.page.forms;

import lombok.Data;
import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.Page;

import java.util.List;

@Page("/selectBoxPlaceholderForeachTag.html")
class SelectBoxPlaceholderForeachTagPage {

    @ModelData
    public List<SelectBoxFormOption> options() {
        return List.of(
                new SelectBoxFormOption("light", "Light"),
                new SelectBoxFormOption("dark", "Dark")
        );
    }

    @FormData("formData")
    public PlaceholderSelectFormModel formData() {
        return new PlaceholderSelectFormModel();
    }

    @Action
    public void submit(@FormData("formData") PlaceholderSelectFormModel formData) {
    }

    @Data
    public static class PlaceholderSelectFormModel {
        private String selectedValue = "";
    }
}
