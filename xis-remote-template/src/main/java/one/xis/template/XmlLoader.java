package one.xis.template;

import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

class XmlLoader {

    private static DocumentBuilder documentBuilder;

    static {
        documentBuilder = documentBuilder();
    }

    Document loadDocument(File file) throws IOException, SAXException {
        return documentBuilder.parse(file);
    }

    Document loadDocument(InputStream in) throws IOException, SAXException {
        return documentBuilder.parse(in);
    }

    @SneakyThrows
    static DocumentBuilder documentBuilder() {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }


}
