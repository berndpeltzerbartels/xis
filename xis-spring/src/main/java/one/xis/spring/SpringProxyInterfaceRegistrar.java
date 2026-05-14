package one.xis.spring;

import one.xis.context.NoProxyFactoryClass;
import one.xis.context.Proxy;
import one.xis.utils.lang.ClassUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.TypeFilter;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.util.List;

class SpringProxyInterfaceRegistrar implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (!(registry instanceof ConfigurableListableBeanFactory beanFactory)) {
            return;
        }
        if (!AutoConfigurationPackages.has(beanFactory)) {
            return;
        }
        registerProxyInterfaces(registry, AutoConfigurationPackages.get(beanFactory));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    }

    private void registerProxyInterfaces(BeanDefinitionRegistry registry, List<String> basePackages) {
        var scanner = proxyInterfaceScanner();
        for (String basePackage : basePackages) {
            for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
                registerProxyInterface(registry, candidate);
            }
        }
    }

    private ClassPathScanningCandidateComponentProvider proxyInterfaceScanner() {
        var scanner = new InterfaceCandidateScanner();
        scanner.addIncludeFilter(proxyAnnotationFilter());
        return scanner;
    }

    private TypeFilter proxyAnnotationFilter() {
        return (metadataReader, metadataReaderFactory) ->
                proxyFactoryClassName(metadataReader.getAnnotationMetadata()) != null;
    }

    private void registerProxyInterface(BeanDefinitionRegistry registry, BeanDefinition candidate) {
        String proxyInterfaceName = candidate.getBeanClassName();
        if (proxyInterfaceName == null) {
            return;
        }
        Class<?> proxyInterface = ClassUtils.classForName(proxyInterfaceName);
        String proxyFactoryClassName = proxyFactoryClassNameForAnnotatedType(proxyInterface);
        if (proxyFactoryClassName == null) {
            return;
        }
        String beanName = beanName(proxyInterface);
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }
        var definition = new GenericBeanDefinition();
        definition.setBeanClass(SpringProxyFactoryBean.class);
        definition.getConstructorArgumentValues().addGenericArgumentValue(proxyInterfaceName);
        definition.getConstructorArgumentValues().addGenericArgumentValue(proxyFactoryClassName);
        definition.setAutowireCandidate(true);
        registry.registerBeanDefinition(beanName, definition);
    }

    private String proxyFactoryClassName(AnnotationMetadata metadata) {
        for (String annotationType : metadata.getAnnotationTypes()) {
            String proxyFactoryClassName = proxyFactoryClassName(annotationType);
            if (proxyFactoryClassName != null) {
                return proxyFactoryClassName;
            }
        }
        return null;
    }

    private String proxyFactoryClassNameForAnnotatedType(Class<?> annotatedType) {
        for (Annotation annotation : annotatedType.getAnnotations()) {
            String proxyFactoryClassName = proxyFactoryClassName(annotation.annotationType());
            if (proxyFactoryClassName != null) {
                return proxyFactoryClassName;
            }
        }
        return null;
    }

    private String proxyFactoryClassName(String annotationTypeName) {
        return proxyFactoryClassName(ClassUtils.classForName(annotationTypeName));
    }

    private String proxyFactoryClassName(Class<?> annotationType) {
        Proxy proxy = annotationType.getAnnotation(Proxy.class);
        if (proxy == null) {
            return null;
        }
        if (!proxy.factory().equals(NoProxyFactoryClass.class)) {
            return proxy.factory().getName();
        }
        if (!proxy.factoryName().isBlank()) {
            return proxy.factoryName();
        }
        throw new IllegalStateException("Proxy annotation " + annotationType.getName()
                + " must define factory or factoryName");
    }

    private String beanName(Class<?> proxyInterface) {
        return Introspector.decapitalize(proxyInterface.getSimpleName());
    }

    private static final class InterfaceCandidateScanner extends ClassPathScanningCandidateComponentProvider {
        private InterfaceCandidateScanner() {
            super(false);
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
        }
    }
}
