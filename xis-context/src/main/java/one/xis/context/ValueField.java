package one.xis.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.utils.lang.TypeUtils;

import java.lang.reflect.Field;

@Slf4j
@RequiredArgsConstructor
class ValueField {
    private final Field field;
    private final Object bean;
    private final String propertyKey;

    void inject() {
        String value = ApplicationProperties.getProperty(propertyKey);
        if (value == null) {
            throw new IllegalStateException("Property '" + propertyKey + "' not found for field " + field.getName() + " in " + bean.getClass().getName());
        }

        try {
            field.setAccessible(true);

            // Type conversion
            Class<?> fieldType = field.getType();
            Object convertedValue = TypeUtils.convertSimple(value, fieldType);

            field.set(bean, convertedValue);
            log.debug("Injected property '{}' = '{}' into {}.{}", propertyKey, value, bean.getClass().getSimpleName(), field.getName());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to inject @Value for field " + field.getName(), e);
        }
    }

    static String extractPropertyKey(String valueExpression) {
        // Support both "${property.name}" and "property.name"
        if (valueExpression.startsWith("${") && valueExpression.endsWith("}")) {
            return valueExpression.substring(2, valueExpression.length() - 1);
        }
        return valueExpression;
    }
}
