package one.xis.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.Include;
import one.xis.Modal;
import one.xis.Page;
import one.xis.Frontlet;
import one.xis.Router;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.context.Inject;
import one.xis.utils.lang.MethodUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
class ClientConfigServiceImpl implements ClientConfigService {

    private final PageAttributesFactory pageAttributesFactory;
    private final FrontletAttributesFactory frontletAttributesFactory;
    private final ComponentHostResolver hostResolver;

    @Inject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;

    @Inject(annotatedWith = Router.class)
    private Collection<Object> routerControllers;

    @Inject(annotatedWith = Frontlet.class)
    private Collection<Object> frontletControllers;

    @Inject(annotatedWith = Modal.class)
    private Collection<Object> modalControllers;

    @Inject(annotatedWith = Include.class)
    private Collection<Object> includes;

    private long pendingEventTtlSeconds = ClientConfigService.PENDING_EVENT_TTL_SECONDS;

    @Getter
    private ClientConfig config;

    @Override
    public void setPendingEventTtlSeconds(long seconds) {
        this.pendingEventTtlSeconds = seconds;
    }

    @Init
    void init() {
        var configBuilder = ClientConfig.builder()
                .pendingEventTtlSeconds(pendingEventTtlSeconds);
        addPageAttributes(configBuilder);
        addFrontletAttributes(configBuilder);
        addIncludeIds(configBuilder);
        config = configBuilder.build();
        log.info("configuration : {}", config);
    }

    private void addPageAttributes(ClientConfig.ClientConfigBuilder configBuilder) {
        var pageIds = new HashSet<String>();
        String welcomePageId = null;
        var pageAttributes = new HashMap<String, PageAttributes>();
        for (Object pageController : pageControllers) {
            var attributes = pageAttributesFactory.attributes(pageController);
            pageAttributes.put(attributes.getNormalizedPath(), attributes);
            pageIds.add(attributes.getNormalizedPath());
            if (attributes.isWelcomePage()) {
                if (welcomePageId != null) {
                    throw new IllegalStateException("There must be exactly one welcome-page (annotated with @WelcomePage). More than one found: " + welcomePageId + ", " + attributes.getNormalizedPath());
                }
                welcomePageId = attributes.getNormalizedPath();
            }
        }
        for (Object routerController : routerControllers) {
            for (var method : MethodUtils.allMethods(routerController)) {
                if (!method.isAnnotationPresent(one.xis.Route.class)) {
                    continue;
                }
                var attributes = pageAttributesFactory.routerAttributes(routerController, method);
                pageAttributes.put(attributes.getNormalizedPath(), attributes);
                pageIds.add(attributes.getNormalizedPath());
            }
        }
        if (welcomePageId == null) {
            // TODO activate this, but special treatment for tests
            //  throw new IllegalStateException("There must be exactly one welcome-page (annotated with @WelcomePage). More than one found: " + welcomePageId + ", " + attributes.getNormalizedPath());
        }
        configBuilder.pageIds(Collections.unmodifiableSet(pageIds))
                .pageAttributes(pageAttributes)
                .welcomePageId(welcomePageId);
    }


    private void addFrontletAttributes(ClientConfig.ClientConfigBuilder configBuilder) {
        var frontletIds = new HashSet<String>();
        var frontletAttributes = new HashMap<String, FrontletAttributes>();
        var controllers = new java.util.ArrayList<Object>();
        controllers.addAll(frontletControllers);
        controllers.addAll(modalControllers);
        for (Object frontletController : controllers) {
            var attributes = frontletAttributesFactory.attributes(frontletController);
            frontletIds.add(attributes.getId());
            frontletAttributes.put(attributes.getId(), attributes); // TODO host
        }
        hostResolver.getFrontletHosts().forEach((frontletId, host) -> {
            if (!frontletAttributes.containsKey(frontletId)) {
                var attributes = new FrontletAttributes();
                attributes.setId(frontletId);
                attributes.setHost(host);
                attributes.setUrl(hostResolver.getFrontletUrls().get(frontletId));
                frontletIds.add(frontletId);
                frontletAttributes.put(frontletId, attributes);
            }
        });
        configBuilder.frontletIds(Collections.unmodifiableSet(frontletIds))
                .frontletAttributes(frontletAttributes);
    }

    private void addIncludeIds(ClientConfig.ClientConfigBuilder configBuilder) {
        var includeIds = includes.stream()
                .map(Object::getClass)
                .map(cls -> cls.getAnnotation(Include.class).value()).collect(Collectors.toSet());
        configBuilder.includeIds(Collections.unmodifiableSet(includeIds));
    }

}
