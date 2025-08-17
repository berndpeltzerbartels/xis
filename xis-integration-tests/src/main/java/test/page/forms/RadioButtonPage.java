package test.page.forms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.Page;

import java.util.List;

@Getter
@Page("/radioButton.html")
@RequiredArgsConstructor
class RadioButtonPage {

    private final RadioButtonFormService service;

    private final List<RadioButtonItem> items = List.of(
            new RadioButtonItem(1, "Option 1"),
            new RadioButtonItem(2, "Option 2"),
            new RadioButtonItem(3, "Option 3")
    );

    @ModelData
    List<RadioButtonItem> items() {
        return items;
    }

    @FormData("form")
    public RadioButtonFormModel getModel() {
        return service.getModel();
    }

    @Action
    public void submit(@FormData("form") RadioButtonFormModel model) {
        service.saveModel(model);
    }
}
