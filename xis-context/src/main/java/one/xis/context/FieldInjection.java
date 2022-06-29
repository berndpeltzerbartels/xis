package one.xis.context;

import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
class FieldInjection {
    private final Set<DependencyField> dependencyFields;

    FieldInjection(AppReflection reflections) {
        dependencyFields = reflections.getFieldsAnnotatedWith(XISInject.class).stream().map(DependencyField::getInstanceForField).collect(Collectors.toSet());
    }

    void onComponentCreated(Object o) {
        dependencyFields.forEach(field -> field.onComponentCreated(o));
    }

    void doInjection() {
        dependencyFields.forEach(DependencyField::doInjection);
    }
}
