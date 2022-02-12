package one.xis.template;

import lombok.Data;

@Data
public class ForLoop {
    private final Expression arraySource;
    private final String itemVarName;
    private final String indexVarName;
    private final String numberVarName;
}
