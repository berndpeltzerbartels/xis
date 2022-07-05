package one.xis.jsc;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ResourceFiles;

import static one.xis.jsc.JavasscriptComponentUtils.getHtmlTemplatePath;

@XISComponent
@RequiredArgsConstructor
class PageFactory {

    private final ResourceFiles resourceFiles;

    Page createPage(@NonNull Object pageController) {
        return new Page(pageController, resourceFiles.getByPath(getHtmlTemplatePath(pageController.getClass())));
    }
}
