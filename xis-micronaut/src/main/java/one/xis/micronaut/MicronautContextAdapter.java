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
import one.xis.context.AppContextBuilder;
import one.xis.server.FrontendService;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@Singleton
class MicronautContextAdapter {

    @Inject
    private BeanContext beanContext;

    @Getter
    private FrontendService frontendService;

    @PostConstruct
    public void init() {
        var context = AppContextBuilder.createInstance()
                .withSingletons(findControllers())
                .withPackage("one.xis")
                .build();
        frontendService = context.getSingleton(FrontendService.class);
    }

    private Collection<Object> findControllers() {
        var controllers = new HashSet<>();
        controllers.addAll(findAnnotatedBeans(Page.class));
        controllers.addAll(findAnnotatedBeans(Widget.class));
        return controllers;
    }

    private <A extends Annotation> Collection<Object> findAnnotatedBeans(Class<A> annotationType) {
        return beanContext.getBeanDefinitions(Qualifiers.byStereotype(annotationType)).stream()//
                .map(BeanDefinition::getBeanType)
                .map(beanContext::getBean)
                .collect(Collectors.toSet());
    }

}
