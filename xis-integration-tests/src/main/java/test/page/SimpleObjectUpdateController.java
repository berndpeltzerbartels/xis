package test.page;


import lombok.RequiredArgsConstructor;
import one.xis.Model;
import one.xis.*;

@Page("/simpleObject/edit/${id}.html")
@HtmlFile("SimpleObjectForm.html")
@RequiredArgsConstructor
class SimpleObjectUpdateController {

    private final SimpleObjectService simpleObjectService;

    @Model("formObject")
    SimpleObject formObject(@URLParameter("id") Integer id) {
        return simpleObjectService.getById(id);
    }

    @Action("save")
    PageResult save(@Model("formObject") SimpleObject simpleObject) {
        simpleObjectService.save(simpleObject);
        return PageResult.of(SimpleObjectDetails.class, "id", simpleObject.getId());
    }

}
