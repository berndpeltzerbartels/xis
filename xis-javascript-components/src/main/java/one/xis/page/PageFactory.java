package one.xis.page;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ResourceFiles;

import static one.xis.jsc.JavascriptComponentUtils.getHtmlTemplatePath;

@XISComponent
@RequiredArgsConstructor
class PageFactory {

    private final ResourceFiles resourceFiles;
    private int nameIndex;

    PageJavascript createPage(@NonNull Object pageController) {
        return new PageJavascript(resourceFiles.getByPath(getHtmlTemplatePath(pageController.getClass())), javascriptClass());
    }

    private String javascriptClass() {
        return "P" + nameIndex++;
    }

}
