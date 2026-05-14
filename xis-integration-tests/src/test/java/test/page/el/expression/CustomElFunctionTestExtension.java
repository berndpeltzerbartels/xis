package test.page.el.expression;

import one.xis.context.IntegrationTestEnvironment;
import one.xis.context.TestScriptExtension;

import java.util.Map;

public class CustomElFunctionTestExtension implements TestScriptExtension {

    @Override
    public String getAdditionalScript() {
        return """
                elFunctions.addFunction('surround', function(left, value, right) {
                    return left + value + right;
                });
                """;
    }

    @Override
    public Map<String, Object> getAdditionalBindings(IntegrationTestEnvironment environment) {
        return Map.of();
    }
}
