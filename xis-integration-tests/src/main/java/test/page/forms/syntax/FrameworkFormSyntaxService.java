package test.page.forms.syntax;

class FrameworkFormSyntaxService {

    private final FrameworkFormSyntaxModel form = new FrameworkFormSyntaxModel();
    private FrameworkFormSyntaxModel saved;

    FrameworkFormSyntaxModel form() {
        form.setName("Original");
        form.setDescription("Existing description");
        form.setCategoryId(1);
        form.setActive(false);
        form.setStatus(null);
        return form;
    }

    void save(FrameworkFormSyntaxModel form) {
        saved = form;
    }

    FrameworkFormSyntaxModel saved() {
        return saved;
    }
}
