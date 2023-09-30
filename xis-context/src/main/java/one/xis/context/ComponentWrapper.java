package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;


@Setter
@Getter
@RequiredArgsConstructor
class ComponentWrapper implements ComponentConsumer, FieldHolder {

    private final Object component;
    private Collection<FieldWrapper> fieldWrappers;
    private Collection<BeanMethodWrapper> beanMethods;
    private Collection<InitMethodWrapper> initMethods;
    private final AppContextFactory contextFactory;

    ComponentWrapper(Object component, ConstructorWrapper constructorWrapper, AppContextFactory contextFactory) {
        this.component = component;
        this.contextFactory = contextFactory;
        fieldWrappers = constructorWrapper.getFieldWrappers();
        beanMethods = constructorWrapper.getBeanMethods();
        initMethods = constructorWrapper.getInitMethods();
        constructorWrapper.getComponentWrapperPlaceholder().setComponentWrapper(this);
        for (var fieldWrapper : fieldWrappers) {
            if (fieldWrapper.isValuesAssigned()) {
                fieldWrapper.inject(component);
                fieldWrappers.remove(fieldWrapper);
            }
        }
        if (fieldWrappers.isEmpty()) {
            executeMethods();
        }
    }

    @Override
    public void fieldValueAssigned(FieldWrapper wrapper) {
        wrapper.inject(component);
        fieldWrappers.remove(wrapper);
    }

    @Override
    public void mapProducers(Collection<ComponentProducer> producers) {
        fieldWrappers.forEach(fieldWrapper -> fieldWrapper.mapProducers(producers));
        initMethods.forEach(initMethods -> initMethods.mapProducers(producers));
        beanMethods.forEach(beanMethods -> beanMethods.mapProducers(producers));
    }

    @Override
    public void mapInitialComponents(Collection<Object> components) {
        fieldWrappers.forEach(fieldWrapper -> fieldWrapper.mapInitialComponents(components));
        initMethods.forEach(initMethods -> initMethods.mapInitialComponents(components));
        beanMethods.forEach(beanMethods -> beanMethods.mapInitialComponents(components));
    }

    void fieldValueFound(FieldWrapper fieldWrapper) {
        fieldWrappers.remove(fieldWrapper);
        fieldWrapper.inject(component);
        if (fieldWrappers.isEmpty()) {
            executeMethods();
        }
    }

    void initMethodParamtersSet(InitMethodWrapper initMethodWrapper) {
        if (fieldWrappers.isEmpty()) {
            initMethods.remove(initMethodWrapper);
            initMethodWrapper.execute(component);
        }
        if (initMethods.isEmpty()) {
            for (var beanMethod : beanMethods) {
                if (beanMethod.isPrepared()) {
                    beanMethods.remove(beanMethod);
                    beanMethod.execute(component);
                }
            }
        }
    }

    void beanMethodParameterSet(BeanMethodWrapper beanMethodWrapper) {
        if (fieldWrappers.isEmpty() && initMethods.isEmpty()) {
            beanMethods.remove(beanMethodWrapper);
            beanMethodWrapper.execute(component);
        }
    }


    boolean isDone() {
        return fieldWrappers.isEmpty() && initMethods.isEmpty() && beanMethods.isEmpty();
    }

    @Override
    public String toString() {
        return "ComponentWrapper{" + component.getClass().getSimpleName() + "}";
    }

    void executeMethods() {
        for (var initMethod : initMethods) {
            if (initMethod.isPrepared()) {
                initMethods.remove(initMethod);
                initMethod.execute(component);
            }
        }
        if (initMethods.isEmpty()) {
            for (var beanMethod : beanMethods) {
                if (beanMethod.isPrepared()) {
                    beanMethods.remove(beanMethod);
                    beanMethod.execute(component);
                }
            }
        }
    }
}
