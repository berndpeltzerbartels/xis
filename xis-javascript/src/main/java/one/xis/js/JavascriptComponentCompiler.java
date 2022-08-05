package one.xis.js;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ReloadableResourceFile;
import one.xis.template.TemplateModel;
import one.xis.template.TemplateSynthaxException;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

@XISComponent
@RequiredArgsConstructor
public abstract class JavascriptComponentCompiler<C extends JavascriptComponent, M extends TemplateModel> {
    public C compileIfObsolete(C pageJavascript) {
        synchronized (pageJavascript) {
            if (!pageJavascript.isCompiled()) {
                compile(pageJavascript);
            }
            if (pageJavascript.getHtmlResourceFile() instanceof ReloadableResourceFile) {
                ReloadableResourceFile reloadableResourceFile = (ReloadableResourceFile) pageJavascript.getHtmlResourceFile();
                if (isObsolete(pageJavascript)) {
                    compile(pageJavascript);
                }
            }
            return pageJavascript;
        }
    }

    public void compile(C pageJavascript) {
        pageJavascript.setCompiled(false);
        reloadHtml(pageJavascript);
        var javascript = doCompile(pageJavascript);
        pageJavascript.setJavascript(javascript);
        pageJavascript.setCompiled(true);
    }

    private String doCompile(C component) {
        var controllerClass = component.getControllerClassName();
        var templateModel = parseWidgetTemplate(controllerClass, htmlToDocument(controllerClass, component.getHtmlResourceFile().getContent()));
        var script = templateModelToScriptModel(templateModel, component.getControllerClassName());
        return javaScriptModelAsCode(script);
    }

    private void reloadHtml(C pageJavascript) {
        if (pageJavascript.getHtmlResourceFile() instanceof ReloadableResourceFile) {
            var reloadableResourceFile = (ReloadableResourceFile) pageJavascript.getHtmlResourceFile();
            reloadableResourceFile.reload();
        } else {
            throw new IllegalStateException();
        }
    }

    private boolean isObsolete(C pageJavascript) {
        if (pageJavascript.getHtmlResourceFile() instanceof ReloadableResourceFile) {
            var reloadableResourceFile = (ReloadableResourceFile) pageJavascript.getHtmlResourceFile();
            return reloadableResourceFile.isObsolete();
        }
        return false;
    }

    protected abstract M parseWidgetTemplate(String controllerClass, Document document);

    protected abstract JSScript templateModelToScriptModel(M templateModel, String javascriptClassName);

    private String javaScriptModelAsCode(@NonNull JSScript script) {
        var builder = new StringBuilder();
        var jsWriter = new JSWriter(builder);
        jsWriter.write(script);
        return builder.toString();
    }

    private Document htmlToDocument(String controllerClass, String htmlSource) {
        try {
            return htmlToDocument(htmlSource);
        } catch (SAXException e) {
            throw new TemplateSynthaxException(controllerClass + ": " + e.getMessage());
        }
    }

    private Document htmlToDocument(String htmlSource) throws SAXException {
        try {
            return XmlUtil.loadDocument(htmlSource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
