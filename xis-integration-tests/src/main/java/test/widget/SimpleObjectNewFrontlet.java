package test.widget;


import lombok.RequiredArgsConstructor;
import one.xis.*;


@Frontlet
@HtmlFile("SimpleObjectFormWidget.html")
@RequiredArgsConstructor
class SimpleObjectNewFrontlet {

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
    FrontletResponse save(@FormData("formObject") SimpleObject simpleObject) {
        simpleObjectService.save(simpleObject);
        return FrontletResponse.of(SimpleObjectDetailsFrontlet.class, "id", simpleObject.getId());
    }

}
