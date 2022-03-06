package one.xis.template;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class IfBlock extends ChildHolderBase implements ModelNode {
    private final Expression expression;
}
