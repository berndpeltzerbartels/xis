package test.widget;


import lombok.RequiredArgsConstructor;
import one.xis.*;


@Widget
@HtmlFile("SimpleObjectFormWidget.html")
@RequiredArgsConstructor
class SimpleObjectNewWidget {

    private final SimpleObjectService simpleObjectService;

    @FormData("formObject")
    SimpleObject simpleObject() {
        return new SimpleObject(1, "Simple Object", "p1", "p2");
    }

    @ModelData("title")
    String title() {
        return "New Object";
    }

    @Action("save")
    WidgetResponse save(@FormData("formObject") SimpleObject simpleObject) {
        simpleObjectService.save(simpleObject);
        return WidgetResponse.of(SimpleObjectDetailsWidget.class, "id", simpleObject.getId());
    }

}
