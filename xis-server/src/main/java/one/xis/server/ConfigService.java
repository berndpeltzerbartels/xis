package one.xis.server;

import lombok.Getter;
import one.xis.Page;
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
                .map(WidgetUtil::getId).collect(Collectors.toSet());

        for (Object pageController : pageControllers) {
            var pageAnno = pageController.getClass().getAnnotation(Page.class);
            pageIds.add(pageAnno.path());
            hosts.put(pageAnno.path(), "");
            if (pageAnno.welcomePage()) {
                welcomePageId = pageAnno.path();
            }
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
