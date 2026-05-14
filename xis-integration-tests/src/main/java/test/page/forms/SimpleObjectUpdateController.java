package test.page.forms;


import lombok.RequiredArgsConstructor;
import one.xis.*;

@Page("/simpleObject/edit/{id}.html")
@HtmlFile("SimpleObjectForm.html")
@RequiredArgsConstructor
class SimpleObjectUpdateController {

    private final SimpleObjectService simpleObjectService;

    @FormData("formObject")
    SimpleObject formObject(@QueryParameter("id") Integer id) {
        return simpleObjectService.getById(id);
    }

    @Action("save")
    PageResponse save(@FormData("formObject") SimpleObject simpleObject) {
        simpleObjectService.save(simpleObject);
        return PageResponse.of(SimpleObjectDetails.class, "id", simpleObject.getId());
    }

}
