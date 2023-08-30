package test.page;


import lombok.RequiredArgsConstructor;
import one.xis.Model;
import one.xis.*;

@Page("/simpleObject/new")
@HtmlFile("SimpleObjectForm.html")
@RequiredArgsConstructor
class SimpleObjectNewController {

    private final SimpleObjectService simpleObjectService;

    @Model("formObject")
    SimpleObject simpleObject() {
        return new SimpleObject();
    }

    @Action("save")
    PageResult save(@Model("formObject") SimpleObject simpleObject) {
        simpleObjectService.save(simpleObject);
        return PageResult.of(SimpleObjectDetails.class, "id", simpleObject.getId());
    }

}
