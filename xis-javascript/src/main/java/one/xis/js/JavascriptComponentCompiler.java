package one.xis.js;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.controller.ControllerUtils;
import one.xis.resource.ReloadableResourceFile;
import one.xis.template.TemplateModel;
import one.xis.template.TemplateSynthaxException;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

@RequiredArgsConstructor
public abstract class JavascriptComponentCompiler<C extends JavascriptComponent, M extends TemplateModel> {

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public C compileIfObsolete(C javascriptComponent) {
        synchronized (javascriptComponent) {
            if (!javascriptComponent.isCompiled()) {
                compile(javascriptComponent);
            }
            if (javascriptComponent.getHtmlResourceFile() instanceof ReloadableResourceFile) {
                if (isObsolete(javascriptComponent)) {
                    compile(javascriptComponent);
                }
            }
            return javascriptComponent;
        }
    }

    public void compile(C javascriptComponent) {
        javascriptComponent.setCompiled(false);
        reloadHtml(javascriptComponent);
        var javascript = doCompile(javascriptComponent);
        javascriptComponent.setJavascript(javascript);
        javascriptComponent.setCompiled(true);
    }

    private String doCompile(C component) {
        var controllerClass = component.getControllerClassName();
        var templateModel = parseTemplate(controllerClass, htmlToDocument(controllerClass, component.getHtmlResourceFile().getContent()));
        var script = new JSScript();
        var javacriptComponentClass = parseTemplateModelIntoScriptModel(templateModel, component.getJavascriptClass(), script);
        addControllerIdField(javacriptComponentClass, component.getControllerClass());
        addMessageAttributes(javacriptComponentClass, component.getControllerClass());
        addComponentIdField(javacriptComponentClass);
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

    protected abstract M parseTemplate(String controllerClass, Document document);

    protected abstract JSClass parseTemplateModelIntoScriptModel(M templateModel, String javascriptClassName, JSScript script);

    protected abstract void addMessageAttributes(JSClass component, Class<?> controllerClass);


    private void addControllerIdField(JSClass component, Class<?> controllerClass) {
        component.addField("controllerId", new JSString(ControllerUtils.getControllerId(controllerClass)));
    }

    private void addComponentIdField(JSClass component) {
        component.addField("componentId", new JSString(component.getClassName()));
    }

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
