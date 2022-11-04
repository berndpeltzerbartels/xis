package one.xis.page;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
class PageJavascripts {

    private final Map<String, PageJavascript> pageJavascriptsByClassname = new HashMap<>();
    private final Map<String, PageJavascript> pageJavascriptsByPath = new HashMap<>();

    private final PageJavascriptCompiler pageJavascriptCompiler;

    @Getter
    private PageJavascript welcomePage;

    PageJavascript createScript(PageMetaData pageMetaData) {
        PageJavascript pageJavascript = createPageJavascript(pageMetaData);
        if (pageMetaData.isWelcomePage()) {
            if (welcomePage != null) {
                throw new IllegalStateException("more then one welcome-page defined (@Page(welcomePage=true))");
            }
            welcomePage = pageJavascript;
        }
        pageJavascriptsByClassname.put(pageMetaData.getControllerClass().getName(), pageJavascript);
        pageJavascriptsByPath.put(pageMetaData.getPath(), pageJavascript);
        return pageJavascript;
    }

    public PageJavascript getByControllerClass(String id) {
        PageJavascript pageJavascript = pageJavascriptsByClassname.get(id);
        synchronized (pageJavascript) {
            pageJavascriptCompiler.compileIfObsolete(pageJavascript);
        }
        return pageJavascript;
    }

    private PageJavascript createPageJavascript(PageMetaData pageMetaData) {
        var pageJavascipt = PageJavascript.builder()
                .htmlResourceFile(pageMetaData.getHtmlTemplate())
                .controllerClass(pageMetaData.getControllerClass())
                .path(pageMetaData.getPath())
                .javascriptClass(pageMetaData.getJavascriptClassname())
                .build();
        pageJavascriptCompiler.compile(pageJavascipt);
        return pageJavascipt;
    }


    Collection<String> getClassnames() {
        return pageJavascriptsByClassname.keySet();
    }

    Map<String, PageJavascript> getPagesByPath() {
        return pageJavascriptsByPath;
    }
}
