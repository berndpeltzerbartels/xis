package one.xis.template;

import lombok.SneakyThrows;
import org.w3c.dom.Element;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class XmlSerializer {
    private static Transformer transformer;

    static {
        transformer = transformer();
    }

    String serialize(Element e) throws TransformerException {
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(e), new StreamResult(writer));
        return writer.toString();
    }

    @SneakyThrows
    private static Transformer transformer() {
        Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        return transformer;
    }

}
