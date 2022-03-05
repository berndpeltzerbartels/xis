package one.xis.template;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ChildHolderBase implements ChildHolder {

    @Getter
    private final List<ModelNode> children = new ArrayList<>();

    @Override
    public void addChild(ModelNode child) {
        children.add(child);
    }

}
