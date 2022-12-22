package one.xis.context;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.mocks.Document;
import one.xis.context.mocks.HttpMock;
import one.xis.context.mocks.LocalStorage;

import javax.script.CompiledScript;


@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class TestFrontendInvoker {

    private final CompiledScript compiledScript;

    @Getter
    private final AppContext appContext;

    @Getter
    private final Document document;

    @Getter
    private final HttpMock http;

    @Getter
    private final LocalStorage localStorage;

    public static TestFrontendInvoker forController(Class<?> controllerClass, AppContext appContext) {
        return new TestFrontendInvokerFactory(controllerClass, appContext).createInvoker();
    }


    public void invokeShow() {

    }
}
