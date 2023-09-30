package one.xis.context;


import lombok.Getter;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.CollectionUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

abstract class ValueHolder implements ComponentCreationListener, ComponentConsumer {

    private final List<Object> values = new ArrayList<>();

    @Getter
    private final Collection<ComponentProducer> componentProducers = new HashSet<>();

    @Override
    public void mapProducers(Collection<ComponentProducer> producers) {
        producers.forEach(producer -> {
            if (producer instanceof ConstructorWrapper) {
                if (getElementType().isAssignableFrom(producer.getResultClass()) && annotationMatches(producer.getResultClass())) {
                    producer.addComponentCreationListener(this);
                    componentProducers.add(producer);
                }
            } else if (ClassUtils.related(producer.getResultClass(), getElementType())) {
                producer.addComponentCreationListener(this);
                componentProducers.add(producer);
            }
        });
    }

    @Override
    public void mapInitialComponents(Collection<Object> components) {
        components.forEach(component -> {
            if (getElementType().isInstance(component) && annotationMatches(component)) {
                values.add(component);
            }
        });
        if (componentProducers.isEmpty()) {
            valueAssigned(value());
        }
    }

    boolean isValuesAssigned() {
        return componentProducers.isEmpty();
    }

    abstract Class<?> getType();

    abstract Class<?> getElementType();

    abstract void valueAssigned(Object o);

    protected Predicate<Class<?>> getAnnotationFilter() {
        return c -> true;
    }

    @Override
    public void componentCreated(Object o, ComponentProducer producer) {
        componentProducers.remove(producer);
        if (!(o instanceof Empty)) {
            if (getElementType().isInstance(o) && annotationMatches(o)) {
                values.add(o);
            }
        }
        if (componentProducers.isEmpty()) {
            valueAssigned(value());
        }
    }

    @SuppressWarnings("unchecked")
    private Object value() {
        if (Collection.class.isAssignableFrom(getType())) {
            return CollectionUtils.elementsOfClass(values, (Class<? extends Collection<?>>) getType());
        }
        if (getType().isArray()) {
            return values.toArray((Object[]) Array.newInstance(getElementType(), values.size()));
        }
        return switch (values.size()) {
            case 0 -> throw new IllegalStateException("no candidate for " + getType());
            case 1 -> values.get(0);
            default -> throw new IllegalStateException("ambigious instances for " + getType() + ":" + values.stream().map(Object::toString).collect(Collectors.joining(", ")));
        };
    }

    private boolean annotationMatches(Object o) {
        return annotationMatches(o.getClass());
    }

    private boolean annotationMatches(Class<?> c) {
        return getAnnotationFilter().test(c);
    }
}
