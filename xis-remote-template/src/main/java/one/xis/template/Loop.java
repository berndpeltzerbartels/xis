package one.xis.template;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class Loop extends ChildHolderBase implements ModelNode {

    private final Expression arraySource;
    private final String itemVarName;
    private final String indexVarName;
    private final String numberVarName;
    private final String raw;

    private final List<ModelNode> children = new ArrayList<>();

    @Override
    public void addChild(ModelNode child) {
        children.add(child);
    }
}
