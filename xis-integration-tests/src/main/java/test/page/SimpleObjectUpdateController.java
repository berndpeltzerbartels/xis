package test.page;


import lombok.RequiredArgsConstructor;
import one.xis.ModelData;
import one.xis.*;

@Page("/simpleObject/edit/${id}.html")
@HtmlFile("SimpleObjectForm.html")
@RequiredArgsConstructor
class SimpleObjectUpdateController {

    private final SimpleObjectService simpleObjectService;

    @ModelData("formObject")
    SimpleObject formObject(@URLParameter("id") Integer id) {
        return simpleObjectService.getById(id);
    }

    @Action("save")
    PageResult save(@ModelData("formObject") SimpleObject simpleObject) {
        simpleObjectService.save(simpleObject);
        return PageResult.of(SimpleObjectDetails.class, "id", simpleObject.getId());
    }

}
