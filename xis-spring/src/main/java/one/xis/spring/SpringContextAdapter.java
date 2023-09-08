package one.xis.spring;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.EnablePushClients;
import one.xis.Page;
import one.xis.Push;
import one.xis.Widget;
import one.xis.context.AppContextBuilder;
import one.xis.server.FrontendService;
import one.xis.server.PushClientProxy;
import one.xis.server.PushClientUtil;
import one.xis.utils.lang.ClassUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@ComponentScan(basePackages = {"one.xis.spring"})
@ServletComponentScan(basePackages = {"one.xis.spring"})
@RequiredArgsConstructor
class SpringContextAdapter implements BeanPostProcessor, ApplicationContextAware {

    private final SpringFilter springFilter;
    private final SpringController springController;

    @Setter
    private ApplicationContext applicationContext;
    private final Collection<Object> controllers = new HashSet<>();
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (isBeanClass(bean.getClass())) {
            controllers.add(bean);
        }
        return bean;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        var context = AppContextBuilder.createInstance()
                .withSingletons(controllers)
                .withSingletonClasses(pushClientClasses())
                .withXIS()
                .build();
        var frontendService = context.getSingleton(FrontendService.class);
        springFilter.setFrontendService(frontendService);
        springController.setFrontendService(frontendService);
        context.getSingletons(PushClientProxy.class).forEach(this::addToSpringContext);
    }

    private Collection<Class<?>> pushClientClasses() {
        return pushClientPackages().flatMap(this::pushClientInterfaces)
                .collect(Collectors.toSet());
    }

    private void addToSpringContext(Object o) {
        var springContext = (ConfigurableApplicationContext) applicationContext;
        springContext.getBeanFactory().registerSingleton(o.getClass().getName(), o);
    }

    private boolean isBeanClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Page.class)
                || clazz.isAnnotationPresent(Widget.class);
    }

    private Stream<String> pushClientPackages() {
        return applicationContext.getBeansWithAnnotation(EnablePushClients.class).values().stream()
                .map(org.springframework.util.ClassUtils::getUserClass)
                .map(clazz -> clazz.getAnnotation(EnablePushClients.class))
                .map(PushClientUtil::packagesToScanForPushClients)
                .flatMap(Set::stream);
    }

    private Stream<Class<?>> pushClientInterfaces(String packageName) {
        var provider = new ClassPathScanningCandidateComponentProvider(false) {

            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return true;
            }
        };
        provider.addIncludeFilter(new AnnotationTypeFilter(Push.class));
        return provider.findCandidateComponents(packageName).stream()
                .map(BeanDefinition::getBeanClassName)
                .map(ClassUtils::classForName);
    }


}
