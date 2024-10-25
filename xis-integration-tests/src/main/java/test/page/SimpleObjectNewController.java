package test.page;


import lombok.RequiredArgsConstructor;
import one.xis.*;

@Page("/simpleObject/new.html")
@HtmlFile("SimpleObjectForm.html")
@RequiredArgsConstructor
class SimpleObjectNewController {

    private final SimpleObjectService simpleObjectService;

    @FormData("formObject")
    SimpleObject simpleObject() {
        return new SimpleObject(1, "Simple Object", "p1", "p2");
    }

    @Action("save")
    PageResponse save(@FormData("formObject") SimpleObject simpleObject) {
        simpleObjectService.save(simpleObject);
        return PageResponse.of(SimpleObjectDetails.class, "id", simpleObject.getId());
    }

}
