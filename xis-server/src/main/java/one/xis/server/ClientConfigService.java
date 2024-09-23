package one.xis.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.Page;
import one.xis.Widget;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.server.ClientConfig.ClientConfigBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

@Slf4j
@XISComponent
@RequiredArgsConstructor
class ClientConfigService {

    private final PageAttributesFactory pageAttributesFactory;
    private final WidgetAttributesFactory widgetAttributesFactory;

    @XISInject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;

    @XISInject(annotatedWith = Widget.class)
    private Collection<Object> widgetControllers;

    @Getter
    private ClientConfig config;

    @XISInit
    void init() {
        var configBuilder = ClientConfig.builder();
        addPageAttributes(configBuilder);
        addWidgetAttributes(configBuilder);
        config = configBuilder.build();
        log.info("configuration : {}", config);
    }

    private void addPageAttributes(ClientConfigBuilder configBuilder) {
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

    private void addWidgetAttributes(ClientConfigBuilder configBuilder) {
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


}
