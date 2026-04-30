package one.xis.spring;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.*;
import one.xis.context.AppContext;
import one.xis.context.AppContextBuilder;
import one.xis.http.RestControllerServiceImpl;
import one.xis.server.FrontendService;
import one.xis.server.ImportedTypes;
import one.xis.server.LocalUrlHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.Collection;
import java.util.HashSet;
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
        var controllerService = context.getSingleton(RestControllerServiceImpl.class);
        springFilter.setFrontendService(frontendService);
        springFilter.setLocalUrlHolder(localUrlHolder);
        springFilter.setRestControllerService(controllerService);
    }
    

    private AppContext createXisContext() {
        return AppContextBuilder.createInstance()
                .withSingletons(singletons)
                .withXIS()
                .build();
    }

    private boolean isFrameworkBeanClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Page.class)
                || clazz.isAnnotationPresent(Widget.class)
                || clazz.isAnnotationPresent(Include.class)
                || isTypeForImport(clazz);
    }

    private static boolean isTypeForImport(Class<?> clazz) {
        return FRAMEWORK_BEAN_CLASSES.stream().anyMatch(c -> c.isAssignableFrom(clazz));
    }
}
