package one.xis.template;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Loop extends ChildHolderBase implements ModelNode, PathNode {

    private final Expression arraySource;
    private final String itemVarName;
    private final String indexVarName;
    private final String numberVarName;
    private final String raw;
}
