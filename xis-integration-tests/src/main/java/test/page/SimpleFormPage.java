package test.page;


import lombok.RequiredArgsConstructor;
import one.xis.Model;
import one.xis.Page;
import one.xis.URLParameter;

@Page("/simpleForm")
@RequiredArgsConstructor
class SimpleFormPage {

    private final SimpleFormService simpleFormService;

    @Model("formObject")
    SimpleFormObject customer(@URLParameter("id") Integer id) {
        return simpleFormService.getById(id);
    }


    @Model("formObject")
    SimpleFormObject newFormObject() {
        return new SimpleFormObject();
    }

}
