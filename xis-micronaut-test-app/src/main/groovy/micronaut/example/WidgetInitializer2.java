package micronaut.example;

import io.micronaut.context.BeanContext;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.discovery.event.ServiceReadyEvent;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.xis.Widget;

import java.util.Collection;
import java.util.stream.Collectors;


//@Singleton
public class WidgetInitializer2 implements ApplicationEventListener<ServiceReadyEvent> {

    @Inject
    private BeanContext beanContext;

    public WidgetInitializer2() {
        System.out.println("created");
    }

    @PostConstruct
    public void init() {
        initializeWidgets(beanContext);
    }

    private void initializeWidgets(BeanContext beanContext) {

        Collection<Object> widgets = beanContext.getBeanDefinitions(Qualifiers.byStereotype(Widget.class)).stream()//
                .map(BeanDefinition::getBeanType)
                .map(beanContext::getBean)
                .collect(Collectors.toSet());
    }

    @Override
    public void onApplicationEvent(ServiceReadyEvent event) {
        Collection<Object> widgets = beanContext.getBeanDefinitions(Qualifiers.byStereotype(Widget.class)).stream()//
                .map(BeanDefinition::getBeanType)
                .map(beanContext::getBean)
                .collect(Collectors.toSet());
    }
}
