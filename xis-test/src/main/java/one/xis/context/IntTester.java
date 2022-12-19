package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.mocks.Document;

import javax.script.CompiledScript;

@RequiredArgsConstructor
public class IntTester {

    private final CompiledScript compiledScript;
    private final AppContext appContext;

    @Getter
    private final Document document;

    public static IntTesterBuilder builder(Class<?> controllerClass) {
        return new IntTesterBuilder(controllerClass);
    }

    public void invokeShow() {

    }
}
