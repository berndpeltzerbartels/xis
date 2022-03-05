package one.xis.template;

import lombok.Data;

@Data
public class IfBlock extends ChildHolderBase implements ModelNode {
    private final Expression expression;
}
