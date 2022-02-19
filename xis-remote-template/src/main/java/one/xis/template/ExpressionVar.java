package one.xis.template;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class ExpressionVar implements ExpressionArg {
    private final String varName;

    public List<String> getPath() {
        return Arrays.asList(varName.split("."));
    }
}
