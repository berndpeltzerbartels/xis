package test.page.forms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.Page;

import java.util.List;

@Getter
@Page("/checkboxList.html")
@RequiredArgsConstructor
class CheckboxListPage {

    private final CheckboxListFormService service;

    private final List<CheckboxListItem> items = List.of(
            new CheckboxListItem(1, "Item 1"),
            new CheckboxListItem(2, "Item 2"),
            new CheckboxListItem(3, "Item 3"),
            new CheckboxListItem(4, "Item 4"),
            new CheckboxListItem(5, "Item 5")
    );

    @ModelData
    List<CheckboxListItem> items() {
        return items;
    }

    @FormData("form")
    public CheckboxListFormModel getModel() {
        return service.getModel();
    }

    @Action
    public void submit(@FormData("form") CheckboxListFormModel model) {
        service.saveModel(model);
    }
}