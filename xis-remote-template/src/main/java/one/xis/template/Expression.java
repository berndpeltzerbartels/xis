package one.xis.template;

import lombok.Data;

import java.util.List;

@Data
public class Expression {
    private final String content;
    private final List<ExpressionArg> vars;
    private final String function;

    @Override
    public String toString() {
        return "<%=" + content + ">";
    }
}
