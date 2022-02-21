package one.xis.template;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
public abstract class ElementBase implements ModelNode {
    private final String elementName;
    private final Map<String, MutableAttribute> mutableAttributes = new HashMap<>();
    private final Map<String, String> staticAttributes = new HashMap<>();
    private IfCondition ifCondition;
    private ForLoop loop;

    void addMutableAttribute(String name, MutableAttribute content) {
        mutableAttributes.put(name, content);
    }

    void addStaticAttribute(String name, String content) {
        staticAttributes.put(name, content);
    }

    boolean isDynamic() {
        return ifCondition != null || loop != null;
    }

    @Override
    public String toString() {
        return "<" + elementName + ">";
    }

}
