package one.xis.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import one.xis.Include;
import one.xis.Page;
import one.xis.Widget;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.context.Inject;

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
    private final WidgetAttributesFactory widgetAttributesFactory;

    @Inject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;

    @Inject(annotatedWith = Widget.class)
    private Collection<Object> widgetControllers;

    @Inject(annotatedWith = Include.class)
    private Collection<Object> includes;

    @Setter
    private boolean useWebsockets;

    @Getter
    private ClientConfig config;

    @Init
    void init() {
        var configBuilder = ClientConfig.builder()
                .useWebsockets(useWebsockets);
        addPageAttributes(configBuilder);
        addWidgetAttributes(configBuilder);
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
        if (welcomePageId == null) {
            // TODO activate this, but special treatment for tests
            //  throw new IllegalStateException("There must be exactly one welcome-page (annotated with @WelcomePage). More than one found: " + welcomePageId + ", " + attributes.getNormalizedPath());
        }
        configBuilder.pageIds(Collections.unmodifiableSet(pageIds))
                .pageAttributes(pageAttributes)
                .welcomePageId(welcomePageId);
    }


    private void addWidgetAttributes(ClientConfig.ClientConfigBuilder configBuilder) {
        var widgetIds = new HashSet<String>();
        var widgetAttributesHashMap = new HashMap<String, WidgetAttributes>();
        for (Object widgetController : widgetControllers) {
            var attributes = widgetAttributesFactory.attributes(widgetController);
            widgetIds.add(attributes.getId());
            widgetAttributesHashMap.put(attributes.getId(), attributes); // TODO host
        }
        configBuilder.widgetIds(Collections.unmodifiableSet(widgetIds))
                .widgetAttributes(widgetAttributesHashMap);
    }

    private void addIncludeIds(ClientConfig.ClientConfigBuilder configBuilder) {
        var includeIds = includes.stream()
                .map(Object::getClass)
                .map(cls -> cls.getAnnotation(Include.class).value()).collect(Collectors.toSet());
        configBuilder.includeIds(Collections.unmodifiableSet(includeIds));
    }

}
