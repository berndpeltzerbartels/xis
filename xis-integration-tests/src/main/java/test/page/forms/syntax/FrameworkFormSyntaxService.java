package test.page.forms.syntax;

import java.util.ArrayList;

class FrameworkFormSyntaxService {

    private final FrameworkFormSyntaxModel form = new FrameworkFormSyntaxModel();
    private FrameworkFormSyntaxModel saved;

    FrameworkFormSyntaxModel form() {
        form.setName("Original");
        form.setDescription("Existing description");
        form.setCategoryId(0);
        form.setActive(false);
        form.setStatus(null);
        form.setChoices(new ArrayList<>());
        return form;
    }

    void save(FrameworkFormSyntaxModel form) {
        saved = form;
    }

    FrameworkFormSyntaxModel saved() {
        return saved;
    }
}
