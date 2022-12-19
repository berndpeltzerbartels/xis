package one.xis.context.mocks;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;


public class Document {

    @Getter(AccessLevel.PACKAGE)
    private final Map<String, Element> elementsById = new HashMap<>();

    @Getter(AccessLevel.PACKAGE)
    private final Map<String, Element> elementsByClass = new HashMap<>();

    public Element createElement(@NonNull String tagname) {
        return new Element(tagname, this);
    }

    public TextNode createTextNode() {
        return new TextNode();
    }


}
