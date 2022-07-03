package one.xis.jscomponent;

import lombok.NonNull;
import one.xis.js.JSScript;
import one.xis.js.JSWriter;
import one.xis.template.TemplateSynthaxException;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

class JavasscriptComponentUtils {

    static String javaScriptModelAsCode(@NonNull JSScript script) {
        StringBuilder builder = new StringBuilder();
        JSWriter jsWriter = new JSWriter(builder);
        jsWriter.write(script);
        return builder.toString();
    }

    static String urnToClassName(String urn) {
        return urn.replace(':', '.');
    }

    static String nameToUrn(String name) {
        return name.replace('.', ':');
    }

    static Document htmlToDocument(String controllerClass, String htmlSource) {
        try {
            return htmlToDocument(htmlSource);
        } catch (SAXException e) {
            throw new TemplateSynthaxException(controllerClass + ": " + e.getMessage());
        }
    }

    static String getHtmlTemplatePath(Class<?> controllerClass) {
        return controllerClass.getName().replace('.', '/') + ".html";
    }

    private static Document htmlToDocument(String htmlSource) throws SAXException {
        try {
            return XmlUtil.loadDocument(htmlSource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
