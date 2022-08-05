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

    private final Map<String, PageJavascript> pageJavascriptsById = new HashMap<>();
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
        pageJavascriptsById.put(pageMetaData.getId(), pageJavascript);
        pageJavascriptsByPath.put(pageMetaData.getPath(), pageJavascript);
        return pageJavascript;
    }

    public PageJavascript getById(String id) {
        PageJavascript pageJavascript = pageJavascriptsById.get(id);
        synchronized (pageJavascript) {
            pageJavascriptCompiler.compileIfObsolete(pageJavascript);
        }
        return pageJavascript;
    }

    public PageJavascript getByPath(String path) {
        PageJavascript pageJavascript = pageJavascriptsByPath.get(path);
        synchronized (pageJavascript) {
            pageJavascriptCompiler.compileIfObsolete(pageJavascript);
        }
        return pageJavascript;
    }

    private PageJavascript createPageJavascript(PageMetaData pageMetaData) {
        var pageJavascipt = new PageJavascript(pageMetaData.getHtmlTemplate(), pageMetaData.getJavascriptClassname(), pageMetaData.getControllerClass());
        pageJavascriptCompiler.compile(pageJavascipt);
        return pageJavascipt;
    }


    Collection<String> getIds() {
        return pageJavascriptsById.keySet();
    }

    Map<String, PageJavascript> getAll() {
        return pageJavascriptsById;
    }
}
