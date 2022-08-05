package one.xis.micronaut;

import io.micronaut.context.BeanContext;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;
import one.xis.Page;
import one.xis.Widget;
import one.xis.context.AppContext;
import one.xis.page.PageService;
import one.xis.root.RootPageService;
import one.xis.widget.WidgetService;

@Singleton
@Getter
public class MicronautContextAdapter {

    @Inject
    private BeanContext beanContext;

    private AppContext appContext;
    private RootPageService rootPageService;
    private PageService pageService;
    private WidgetService widgetService;

    @PostConstruct
    void init() {
        appContext = AppContext.getInstance("one.xis");
        rootPageService = appContext.getSingleton(RootPageService.class);
        pageService = appContext.getSingleton(PageService.class);
        widgetService = appContext.getSingleton(WidgetService.class);

        beanContext.getBeanDefinitions(Qualifiers.byStereotype(Page.class)).stream()//
                .map(BeanDefinition::getBeanType)
                .map(beanContext::getBean)
                .forEach(bean -> pageService.addPageController(bean));

        beanContext.getBeanDefinitions(Qualifiers.byStereotype(Widget.class)).stream()//
                .map(BeanDefinition::getBeanType)
                .map(beanContext::getBean)
                .forEach(bean -> widgetService.addWidgetConroller(bean));
    }
}
