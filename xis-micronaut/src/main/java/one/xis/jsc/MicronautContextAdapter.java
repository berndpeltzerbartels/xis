package one.xis.jsc;

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
import one.xis.resource.ResourceFiles;

@Singleton
@Getter
class MicronautContextAdapter {

    @Inject
    private BeanContext beanContext;

    private AppContext appContext;
    private Pages pages;
    private Widgets widgets;
    private RootPage rootPage;
    private ResourceFiles resourceFiles;

    @PostConstruct
    void init() {
        appContext = AppContext.getInstance("one.xis");
        pages = appContext.getSingleton(Pages.class);
        widgets = appContext.getSingleton(Widgets.class);
        rootPage = appContext.getSingleton(RootPage.class);
        resourceFiles = appContext.getSingleton(ResourceFiles.class);

        beanContext.getBeanDefinitions(Qualifiers.byStereotype(Page.class)).stream()//
                .map(BeanDefinition::getBeanType)
                .map(beanContext::getBean)
                .forEach(bean -> pages.add(bean.getClass().getName(), bean));

        beanContext.getBeanDefinitions(Qualifiers.byStereotype(Widget.class)).stream()//
                .map(BeanDefinition::getBeanType)
                .map(beanContext::getBean)
                .forEach(bean -> widgets.add(bean.getClass().getName(), bean));
    }


}
