package test.page.forms.syntax;

import one.xis.Action;
import one.xis.ActionParameter;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.Page;

import java.util.List;

@Page("/frameworkFormSyntax.html")
class FrameworkFormSyntaxPage {

    private final FrameworkFormSyntaxService service;

    FrameworkFormSyntaxPage(FrameworkFormSyntaxService service) {
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
    public void save(@FormData("form") FrameworkFormSyntaxModel form,
                     @ActionParameter("submitMode") String submitMode) {
        service.save(form, submitMode);
    }
}
