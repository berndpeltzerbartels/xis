package one.xis.context;

import lombok.Getter;
import one.xis.ajax.AjaxService;
import one.xis.context.mocks.Document;
import one.xis.context.mocks.HttpMock;
import one.xis.context.mocks.LocalStorage;

import javax.script.CompiledScript;


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

    TestFrontendInvoker(CompiledScript compiledScript, AppContext appContext) {
        this.compiledScript = compiledScript;
        this.appContext = appContext;
        this.document = new Document();
        this.localStorage = new LocalStorage();
        this.http = new HttpMock(appContext.getSingleton(AjaxService.class));
    }
    
    public void invokeShow() {

    }
}
