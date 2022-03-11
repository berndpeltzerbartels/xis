package one.xis.template;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class ElementBase extends ChildHolderBase implements ModelNode, ElementWithAttributes {
    private final String elementName;
    private final Map<String, MutableAttribute> mutableAttributes = new HashMap<>();
    private final Map<String, String> staticAttributes = new HashMap<>();

    void addMutableAttribute(String name, MutableAttribute content) {
        mutableAttributes.put(name, content);
    }

    void addStaticAttribute(String name, String content) {
        staticAttributes.put(name, content);
    }

    @Override
    public String toString() {
        return "<" + elementName + ">";
    }

}
