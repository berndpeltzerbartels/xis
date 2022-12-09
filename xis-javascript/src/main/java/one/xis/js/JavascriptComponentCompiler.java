package one.xis.js;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.resource.ReloadableResource;
import one.xis.template.TemplateDocumentParser;
import one.xis.template.TemplateModel;
import one.xis.template.TemplateSynthaxException;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public abstract class JavascriptComponentCompiler<C extends JavascriptComponent, M extends TemplateModel> {

    private final TemplateDocumentParser<M> documentParser;
    private final JavascriptTemplateParser<M> templateParser;
    private final JavascriptControllerModelParser controllerModelParser;

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public C compileIfObsolete(C javascriptComponent) {
        synchronized (javascriptComponent) {
            if (!javascriptComponent.isCompiled()) {
                compile(javascriptComponent);
            }
            if (javascriptComponent.getHtmlResource() instanceof ReloadableResource) {
                var reloadableResourceFile = (ReloadableResource) javascriptComponent.getHtmlResource();
                if (reloadableResourceFile.isObsolete()) {
                    reloadableResourceFile.reload();
                    compile(javascriptComponent);
                }
            }
            return javascriptComponent;
        }
    }

    public void compile(C javascriptComponent) {
        javascriptComponent.setCompiled(false);
        var javascriptModel = doCompile(javascriptComponent);
        javascriptComponent.setJavascript(javaScriptModelAsCode(javascriptModel));
        javascriptComponent.setCompiled(true);
    }

    public JSScript doCompile(C component) {
        log.info("compile template for {}", component.getControllerClass());
        var controllerClass = component.getControllerClassName();
        var templateModel = parseTemplate(controllerClass, htmlToDocument(controllerClass, component.getHtmlResource().getContent()));
        var script = new JSScript();
        var javacriptComponentClass = parseTemplateModelIntoScriptModel(templateModel, component.getJavascriptClass(), script);
        addControllerIdField(javacriptComponentClass, component.getControllerClass());
        addMessageAttributes(javacriptComponentClass, component.getControllerClass());
        addComponentIdField(javacriptComponentClass);
        return script;
    }


    private M parseTemplate(String controllerClass, Document document) {
        return documentParser.parseTemplate(document, controllerClass);
    }

    private JSClass parseTemplateModelIntoScriptModel(M templateModel, String javascriptClassName, JSScript script) {
        return templateParser.parseTemplateModel(templateModel, javascriptClassName, script);
    }

    private void addMessageAttributes(JSClass component, Class<?> controllerClass) {
        controllerModelParser.parseControllerModel(controllerClass, component);
    }

    private void addControllerIdField(JSClass component, Class<?> controllerClass) {
        component.addField("controllerClass", new JSString(controllerClass.getName()));
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
