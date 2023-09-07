package one.xis.micronaut;


import io.micronaut.context.BeanContext;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;
import one.xis.EnablePushClients;
import one.xis.Page;
import one.xis.Push;
import one.xis.Widget;
import one.xis.context.AppContextBuilder;
import one.xis.server.FrontendService;
import one.xis.server.PushClientUtil;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                .withSingletonClasses(findPushClientInterfaces())
                .withXIS()
                .build();
        frontendService = context.getSingleton(FrontendService.class);
    }

    private Collection<Object> findControllers() {
        var controllers = new HashSet<>();
        controllers.addAll(findAnnotatedBeans(Page.class));
        controllers.addAll(findAnnotatedBeans(Widget.class));
        return controllers;
    }

    private Collection<Class<?>> findPushClientInterfaces() {
        return findAnnotatedBeanTypes(EnablePushClients.class)
                .map(Object::getClass)
                .map(clazz -> clazz.getAnnotation(EnablePushClients.class))
                .map(PushClientUtil::packagesToScanForPushClients)
                .flatMap(Set::stream)
                .flatMap(this::findPushClientInterfaces)
                .collect(Collectors.toSet());
    }

    private Stream<Class<?>> findPushClientInterfaces(String packageName) {
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(), new TypeAnnotationsScanner());
        return reflections.getTypesAnnotatedWith(Push.class).stream();
    }

    private <A extends Annotation> Collection<Object> findAnnotatedBeans(Class<A> annotationType) {
        return findAnnotatedBeanTypes(annotationType)
                .map(beanContext::getBean)
                .collect(Collectors.toSet());
    }

    private <A extends Annotation> Stream<Class<?>> findAnnotatedBeanTypes(Class<A> annotationType) {
        return beanContext.getBeanDefinitions(Qualifiers.byStereotype(annotationType)).stream()//
                .map(BeanDefinition::getBeanType);
    }

}
