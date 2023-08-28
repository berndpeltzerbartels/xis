package test.page;


import lombok.RequiredArgsConstructor;
import one.xis.*;

@Page("/simpleObject/new")
@HtmlFile("SimpleObjectForm.html")
@RequiredArgsConstructor
class SimpleObjectNewController {

    private final SimpleObjectService simpleObjectService;

    @FormData("formObject")
    SimpleObject simpleObject() {
        return new SimpleObject();
    }

    @FormAction("save")
    PageResult save(@FormData("formObject") SimpleObject simpleObject) {
        simpleObjectService.save(simpleObject);
        return PageResult.of(SimpleObjectDetails.class, "id", simpleObject.getId());
    }

}
