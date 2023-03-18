package one.xis.utils.xml;

import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

class XmlLoader {

    Document loadDocument(File file) throws IOException, SAXException {
        return documentBuilder().parse(file);
    }

    Document loadDocument(InputStream in) throws IOException, SAXException {
        return documentBuilder().parse(in);
    }

    Document loadDocument(String xml) throws IOException, SAXException {
        try (StringReader in = new StringReader(xml)) {
            return documentBuilder().parse(new InputSource(in));
        }
    }

    @SneakyThrows
    private DocumentBuilder documentBuilder() {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }


}
