package one.xis.spring;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.EnablePushClients;
import one.xis.Page;
import one.xis.Push;
import one.xis.Widget;
import one.xis.context.AppContext;
import one.xis.context.AppContextBuilder;
import one.xis.http.ControllerService;
import one.xis.server.*;
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


@Getter
@Configuration
@ComponentScan(basePackages = {"one.xis.spring"})
@ServletComponentScan(basePackages = {"one.xis.spring"})
@RequiredArgsConstructor
public class SpringContextAdapter implements BeanPostProcessor, ApplicationContextAware, ResourcePathProvider {

    private final SpringFilter springFilter;
    private AppContext context;

    @Setter
    private ApplicationContext applicationContext;
    private final Collection<Object> singletons = new HashSet<>();

    private static final Set<Class<?>> FRAMEWORK_BEAN_CLASSES = ImportedTypes.getImportedTypes();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (isFrameworkBeanClass(bean.getClass())) {
            singletons.add(bean);
        }
        return bean;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        singletons.add(this);
        context = createXisContext();
        var frontendService = context.getSingleton(FrontendService.class);
        var localUrlHolder = context.getSingleton(LocalUrlHolder.class);
        var controllerService = context.getSingleton(ControllerService.class);
        springFilter.setFrontendService(frontendService);
        springFilter.setLocalUrlHolder(localUrlHolder);
        springFilter.setControllerService(controllerService);
        context.getSingletons(PushClientProxy.class).forEach(this::addToSpringContext);
    }


    public void importInstances() {

    }

    private AppContext createXisContext() {
        return AppContextBuilder.createInstance()
                .withSingletons(singletons)
                .withSingletonClasses(pushClientClasses())
                .withXIS()
                .build();
    }

    private Collection<Class<?>> pushClientClasses() {
        return pushClientPackages().flatMap(this::pushClientInterfaces)
                .collect(Collectors.toSet());
    }

    private void addToSpringContext(Object o) {
        var springContext = (ConfigurableApplicationContext) applicationContext;
        springContext.getBeanFactory().registerSingleton(o.getClass().getName(), o);
    }

    private boolean isFrameworkBeanClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Page.class)
                || clazz.isAnnotationPresent(Widget.class)
                || isTypeForImport(clazz);
    }

    private static boolean isTypeForImport(Class<?> clazz) {
        return FRAMEWORK_BEAN_CLASSES.stream().anyMatch(c -> c.isAssignableFrom(clazz));
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


    @Override
    public String getCustomStaticResourcePath() {
        return "public";
    }
}
