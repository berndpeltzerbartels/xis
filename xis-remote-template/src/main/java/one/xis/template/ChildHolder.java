package one.xis.template;

import java.util.List;

public interface ChildHolder extends ModelNode {
    void addChild(ModelNode child);

    List<ModelNode> getChildren();
}
