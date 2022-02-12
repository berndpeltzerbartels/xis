package one.xis.template;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ModelElement implements ModelNode {
    private final String elementName;
    private final Map<String, MutableAttribute> mutableAttributes = new HashMap<>();
    private final Map<String, String> staticAttributes = new HashMap<>();
    private IfCondition ifCondition;
    private ForLoop loop;
    private final List<ModelNode> children = new ArrayList<>();

    void addChild(ModelNode child) {
        children.add(child);
    }

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
