package one.xis.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.Page;
import one.xis.WelcomePage;
import one.xis.Widget;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.server.Config.ConfigBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

@XISComponent
@RequiredArgsConstructor
class ConfigService {

    private final ComponentAttributesFactory componentAttributesFactory;

    @XISInject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;

    @XISInject(annotatedWith = Widget.class)
    private Collection<Object> widgetControllers;


    @Getter
    private Config config;

    @XISInit
    void init() {
        ConfigBuilder configBuilder = Config.builder();
        addPageAttributes(configBuilder);
        addWidgetAttributes(configBuilder);
        config = configBuilder.build();
    }

    private void addPageAttributes(ConfigBuilder configBuilder) {
        var pageHosts = new HashMap<String, String>();
        var pageIds = new HashSet<String>();
        var pageAttributes = new HashMap<String, ComponentAttributes>();
        String welcomePageId = null;
        for (Object pageController : pageControllers) {
            var pageAnno = pageController.getClass().getAnnotation(Page.class);
            pageIds.add(pageAnno.value());
            pageHosts.put(pageAnno.value(), "");
            pageAttributes.put(pageAnno.value(), componentAttributesFactory.componentAttributes(pageController));
            if (pageController.getClass().isAnnotationPresent(WelcomePage.class)) {
                if (welcomePageId != null) {
                    throw new IllegalStateException("There must be exactly one welcome-page (annotated with @WelcomePage). More than one found: " + welcomePageId + ", " + pageAnno.value());
                }
                welcomePageId = pageAnno.value();
            }

        }
        configBuilder.pageHosts(Collections.unmodifiableMap(pageHosts))
                .pageIds(Collections.unmodifiableSet(pageIds))
                .pageAttributes(pageAttributes)
                .welcomePageId(welcomePageId);
    }

    private void addWidgetAttributes(ConfigBuilder configBuilder) {
        var widgetHosts = new HashMap<String, String>();
        var widgetIds = new HashSet<String>();
        var widgetAttributes = new HashMap<String, ComponentAttributes>();
        for (Object widgetController : widgetControllers) {
            var id = WidgetUtil.getId(widgetController);
            widgetIds.add(id);
            widgetHosts.put(id, "");
            widgetAttributes.put(id, componentAttributesFactory.componentAttributes(widgetController));

        }
        configBuilder.widgetHosts(Collections.unmodifiableMap(widgetHosts))
                .widgetIds(Collections.unmodifiableSet(widgetIds))
                .widgetAttributes(widgetAttributes);
    }


}
