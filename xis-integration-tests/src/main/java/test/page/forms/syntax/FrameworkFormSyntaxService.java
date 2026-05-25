package test.page.forms.syntax;

import java.util.ArrayList;

class FrameworkFormSyntaxService {

    private final FrameworkFormSyntaxModel form = new FrameworkFormSyntaxModel();
    private FrameworkFormSyntaxModel saved;
    private String submitMode;

    FrameworkFormSyntaxModel form() {
        form.setName("Original");
        form.setDescription("Existing description");
        form.setCategoryId(0);
        form.setActive(false);
        form.setStatus(null);
        form.setChoices(new ArrayList<>());
        return form;
    }

    void save(FrameworkFormSyntaxModel form, String submitMode) {
        saved = form;
        this.submitMode = submitMode;
    }

    FrameworkFormSyntaxModel saved() {
        return saved;
    }

    String submitMode() {
        return submitMode;
    }
}
