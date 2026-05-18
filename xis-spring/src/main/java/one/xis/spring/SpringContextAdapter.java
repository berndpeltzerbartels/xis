package one.xis.spring;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.Frontlet;
import one.xis.Include;
import one.xis.Modal;
import one.xis.Page;
import one.xis.RefreshEvent;
import one.xis.RefreshEventPublisher;
import one.xis.context.AppContext;
import one.xis.context.AppContextBuilder;
import one.xis.http.Controller;
import one.xis.http.RestControllerServiceImpl;
import one.xis.server.FrontendService;
import one.xis.server.ImportedTypes;
import one.xis.server.LocalUrlHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


@Getter
@Configuration
@ComponentScan(basePackages = {"one.xis.spring"})
@ServletComponentScan(basePackages = {"one.xis.spring"})
@RequiredArgsConstructor
public class SpringContextAdapter implements BeanPostProcessor, ApplicationContextAware {

    private final SpringFilter springFilter;
    private AppContext context;

    @Setter
    private ApplicationContext applicationContext;
    private final Collection<Object> singletons = new HashSet<>();

    private Set<Class<?>> frameworkBeanClasses;

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
        var controllerService = context.getSingleton(RestControllerServiceImpl.class);
        springFilter.setFrontendService(frontendService);
        springFilter.setLocalUrlHolder(localUrlHolder);
        springFilter.setRestControllerService(controllerService);
    }

    @Bean
    public RefreshEventPublisher refreshEventPublisher() {
        return this::publishRefreshEvent;
    }


    private AppContext createXisContext() {
        return AppContextBuilder.createInstance()
                .withSingletons(singletons)
                .withSingletonClasses(httpControllerClasses())
                .withXIS()
                .build();
    }

    private void publishRefreshEvent(RefreshEvent refreshEvent) {
        if (context == null) {
            throw new IllegalStateException("XIS context is not initialized yet");
        }
        context.getSingleton(RefreshEventPublisher.class).publish(refreshEvent);
    }

    private boolean isFrameworkBeanClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Page.class)
                || clazz.isAnnotationPresent(Frontlet.class)
                || clazz.isAnnotationPresent(Modal.class)
                || clazz.isAnnotationPresent(Include.class)
                || DataSource.class.isAssignableFrom(clazz)
                || isTypeForImport(clazz);
    }

    private boolean isTypeForImport(Class<?> clazz) {
        return getFrameworkBeanClasses().stream().anyMatch(c -> c.isAssignableFrom(clazz));
    }

    private Set<Class<?>> getFrameworkBeanClasses() {
        if (frameworkBeanClasses == null) {
            frameworkBeanClasses = ImportedTypes.getImportedTypes();
        }
        return frameworkBeanClasses;
    }

    private Set<Class<?>> httpControllerClasses() {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.setResourceLoader(applicationContext);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));
        var classes = new LinkedHashSet<Class<?>>();
        springBootBasePackages().forEach(basePackage -> scanner.findCandidateComponents(basePackage).stream()
                .map(this::classForBeanDefinition)
                .filter(clazz -> singletons.stream().noneMatch(singleton -> singleton.getClass().equals(clazz)))
                .forEach(classes::add));
        return classes;
    }

    private Set<String> springBootBasePackages() {
        var packages = new LinkedHashSet<String>();
        var beanFactory = applicationContext.getAutowireCapableBeanFactory();
        if (AutoConfigurationPackages.has(beanFactory)) {
            packages.addAll(AutoConfigurationPackages.get(beanFactory));
        }
        return packages;
    }

    private Class<?> classForBeanDefinition(BeanDefinition beanDefinition) {
        try {
            return Class.forName(beanDefinition.getBeanClassName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not load XIS HTTP controller class " + beanDefinition.getBeanClassName(), e);
        }
    }
}
