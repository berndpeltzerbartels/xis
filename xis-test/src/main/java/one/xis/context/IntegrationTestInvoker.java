package one.xis.context;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import one.xis.context.mocks.Document;
import one.xis.context.mocks.HttpMock;
import one.xis.context.mocks.LocalStorage;
import one.xis.js.JSUtil;

import javax.script.ScriptException;
import java.util.Map;


@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class IntegrationTestInvoker implements AppContext {

    static final String TEXT_OBJECT_VAR_NAME = "testObject";

    private final String script;

    private final Map<String, Object> bindings;

    @Delegate
    private final AppContext appContext;

    @Getter
    private final Document document;

    @Getter
    private final HttpMock http;

    @Getter
    private final LocalStorage localStorage;

    public static IntegrationTestInvokerBuilder builder(Class<?> controllerClass) {
        return new IntegrationTestInvokerBuilder(controllerClass);
    }

    public void invokeInit() throws ScriptException {
        var invokerScript = script + TEXT_OBJECT_VAR_NAME + ".init();";
        JSUtil.compile(invokerScript, bindings).eval();
    }

    public void invokeShow() throws ScriptException {
        var invokerScript = script + TEXT_OBJECT_VAR_NAME + ".show();";
        JSUtil.compile(invokerScript, bindings).eval();
    }
}
