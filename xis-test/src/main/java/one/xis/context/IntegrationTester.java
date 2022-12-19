package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.mocks.Document;

import javax.script.CompiledScript;

@RequiredArgsConstructor
public class IntegrationTester {

    private final CompiledScript compiledScript;
    private final AppContext appContext;

    @Getter
    private final Document document;

    public static IntegrationTesterBuilder builder(Class<?> controllerClass) {
        return new IntegrationTesterBuilder(controllerClass);
    }

    public void invokeShow() {

    }
}
