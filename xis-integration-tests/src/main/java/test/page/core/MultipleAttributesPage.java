package test.page.core;

import lombok.Getter;
import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;

import java.util.ArrayList;
import java.util.List;

@Page("/multipleAttributesPage.html")
class MultipleAttributesPage {

    @Getter
    private final List<String> invokedActions = new ArrayList<>();

    @ModelData("items")
    List<MultipleAttributesItem> actions() {
        return List.of(new MultipleAttributesItem("id1", "action1"),
                new MultipleAttributesItem("id2", "action2"),
                new MultipleAttributesItem("id3", "action3"));
    }

    @Action("action1")
    void action1() {
        invokedActions.add("action1");
    }

    @Action("action2")
    void action2() {
        invokedActions.add("action2");
    }

    @Action("action3")
    void action3() {
        invokedActions.add("action3");
    }
}
