package test.page.forms.syntax;

import java.util.List;

final class FrameworkFormSyntaxOptions {

    private FrameworkFormSyntaxOptions() {
    }

    static List<FrameworkFormSyntaxOption> categories() {
        return List.of(
                new FrameworkFormSyntaxOption(1, "Drafts"),
                new FrameworkFormSyntaxOption(2, "Published")
        );
    }
}
