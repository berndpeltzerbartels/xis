package one.xis.utils.xml;

import lombok.SneakyThrows;
import org.w3c.dom.Element;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

public class XmlSerializer {

    String serialize(Element element) throws TransformerException {
        var transformer = transformer();
        try (StringWriter writer = new StringWriter()) {
            transformer.transform(new DOMSource(element), new StreamResult(writer));
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private static Transformer transformer() {
        Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        return transformer;
    }

}
