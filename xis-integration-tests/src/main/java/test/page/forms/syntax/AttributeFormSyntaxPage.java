package test.page.forms.syntax;

import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.Page;

import java.util.List;

@Page("/attributeFormSyntax.html")
class AttributeFormSyntaxPage {

    private final FrameworkFormSyntaxService service;

    AttributeFormSyntaxPage(FrameworkFormSyntaxService service) {
        this.service = service;
    }

    @FormData("form")
    public FrameworkFormSyntaxModel form() {
        return service.form();
    }

    @ModelData
    public List<FrameworkFormSyntaxOption> categories() {
        return FrameworkFormSyntaxOptions.categories();
    }

    @Action
    public void save(@FormData("form") FrameworkFormSyntaxModel form) {
        service.save(form);
    }
}
