package one.xis.context.mocks;

import lombok.Getter;
import lombok.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Document {

    private final Map<String, Element> elementsById = new HashMap<>();

    private final Map<String, Collection<Element>> elementsByTagName = new HashMap<>();

    private final Map<String, Element> elementsByClass = new HashMap<>();

    public Element createElement(@NonNull String tagname) {
        return new Element(tagname, this);
    }

    public TextNode createTextNode(String text) {
        return new TextNode(text);
    }
    

}
