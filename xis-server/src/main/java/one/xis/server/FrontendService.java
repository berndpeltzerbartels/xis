package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.parameter.UserContext;
import one.xis.resource.Resource;
import one.xis.resource.Resources;
import one.xis.validation.ValidatorMessages;
import org.tinylog.Logger;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Encapsulates all methods, required by the framework's controller.
 */
@XISComponent
@RequiredArgsConstructor
public class FrontendService {

    private final PageControllerService pageControllerService;
    private final WidgetControllerService widgetControllerService;
    private final ClientConfigService configService;
    private final HtmlResourceService htmlResourceService;
    private final Resources resources;
    private final RequestFilters requestFilterChain;
    private final DataSerializer dataSerializer;
    private Resource appJsResource;
    private Resource classesJsResource;
    private Resource mainJsResource;
    private Resource functionsJsResource;

    @XISInit
    void init() {
        appJsResource = resources.getByPath("app.js");
        classesJsResource = resources.getByPath("classes.js");
        mainJsResource = resources.getByPath("main.js");
        functionsJsResource = resources.getByPath("functions.js");
    }

    public ClientConfig getConfig() {
        return configService.getConfig();
    }

    public ServerResponse processPageActionRequest(ClientRequest request) {
        try {
            addUserContext(request);
            return applyFilterChain(request, pageControllerService::processPageActionRequest);
        } finally {
            removeUserContext();
        }
    }

    public ServerResponse processWidgetActionRequest(ClientRequest request) {
        try {
            addUserContext(request);
            return applyFilterChain(request, widgetControllerService::processWidgetActionRequest);
        } finally {
            removeUserContext();
        }
    }

    public ServerResponse processPageModelDataRequest(ClientRequest request) {
        try {
            addUserContext(request);
            return applyFilterChain(request, pageControllerService::processPageModelDataRequest);
        } finally {
            removeUserContext();
        }
    }

    public ServerResponse processWidgetModelDataRequest(ClientRequest request) {
        try {
            addUserContext(request);
            return applyFilterChain(request, widgetControllerService::processWidgetModelDataRequest);
        } finally {
            removeUserContext();
        }
    }

    public String getPage(String id) {
        return htmlResourceService.getPage(id);
    }

    public String getPageHead(String id) {
        var head = htmlResourceService.getPageHead(id);
        Logger.info(head);
        return head;
    }

    public String getPageBody(String id) {
        var body = htmlResourceService.getPageBody(id);
        Logger.info(body);
        return body;
    }

    public Map<String, String> getBodyAttributes(String id) {
        return htmlResourceService.getBodyAttributes(id);
    }

    public String getWidgetHtml(String id) {
        return htmlResourceService.getWidgetHtml(id);
    }

    public String getRootPageHtml() {
        return htmlResourceService.getRootPageHtml();
    }


    public String getAppJs() {
        return appJsResource.getContent();
    }

    public String getClassesJs() {
        return classesJsResource.getContent();
    }

    public String getMainJs() {
        return mainJsResource.getContent();
    }

    public String getFunctionsJs() {
        return functionsJsResource.getContent();
    }

    private void addUserContext(ClientRequest request) {
        var userContext = new UserContext();
        userContext.setClientId(request.getClientId());
        userContext.setUserId(request.getUserId());
        userContext.setLocale(request.getLocale());
        userContext.setZoneId(ZoneId.of(request.getZoneId()));
        UserContext.setInstance(userContext);
    }

    void removeUserContext() {
        UserContext.removeInstance();
    }

    private ServerResponse applyFilterChain(ClientRequest request, Function<ClientRequest, ServerResponse> responder) {
        var validationResult = new ValidatorMessages();
        var chain = requestFilterChain.apply(request, validationResult);
        if (chain.isInterrupt()) {
            return new ServerResponse(chain.getHttpStatus(), dataSerializer.serialize(chain.getData()), null, null, new HashMap<>(), validationResult);
        }
        return responder.apply(request);
    }

}
