package one.xis.server;

import lombok.Getter;
import one.xis.Page;
import one.xis.WelcomePage;
import one.xis.Widget;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

@XISComponent
class ConfigService {

    @XISInject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;

    @XISInject(annotatedWith = Widget.class)
    private Collection<Object> widgetControllers;


    @Getter
    private Config config;

    @XISInit
    void init() {
        var hosts = new HashMap<String, String>();
        var pageIds = new HashSet<String>();
        String welcomePageId = null;
        var widgeIds = widgetControllers.stream()
                .map(WidgetUtil::getId)
                .collect(Collectors.toSet());

        for (Object pageController : pageControllers) {
            var pageAnno = pageController.getClass().getAnnotation(Page.class);
            pageIds.add(pageAnno.value());
            hosts.put(pageAnno.value(), "");
            if (pageController.getClass().isAnnotationPresent(WelcomePage.class)) {
                if (welcomePageId != null) {
                    throw new IllegalStateException("There must be exactly one welcome-page (@Page(welcomePage = true)). More than one found " + welcomePageId + ", " + pageAnno.value());
                }
                welcomePageId = pageAnno.value();
            }
        }

        if (welcomePageId == null) {
            throw new IllegalStateException("There must be exactly one welcome-page (annotated with @Welcome), but no one was found");
        }
        config = Config.builder()
                .pageHosts(Collections.unmodifiableMap(hosts))
                .pageIds(Collections.unmodifiableSet(pageIds))
                .widgetHosts(Collections.emptyMap())
                .widgetIds(Collections.unmodifiableSet(widgeIds))
                .welcomePageId(welcomePageId)
                .build();
    }


}
