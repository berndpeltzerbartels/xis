package one.xis.template;

import java.util.List;

public interface ChildHolder {
    void addChild(ModelNode child);

    List<ModelNode> getChildren();
}
