package one.xis.validation;

import lombok.NonNull;
import one.xis.UserContext;
import one.xis.context.Component;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Map;

@Component
class MinLengthValidator implements Validator<Object> {

    @Override
    public void validate(@NonNull Object value, @NonNull AnnotatedElement annotatedElement, @NonNull UserContext userContext) throws ValidatorException {
        int minLength = annotatedElement.getAnnotation(MinLength.class).value();
        if (value instanceof String str && str.length() < minLength) {
            throw new ValidatorException();
        } else if (value instanceof Collection<?> collection && collection.size() < minLength) {
            throw new ValidatorException();
        } else if (value instanceof Object[] array && array.length < minLength) {
            throw new ValidatorException();
        } else if (value instanceof int[] intArray && intArray.length < minLength) {
            throw new ValidatorException();
        } else if (value instanceof long[] longArray && longArray.length < minLength) {
            throw new ValidatorException();
        } else if (value instanceof double[] doubleArray && doubleArray.length < minLength) {
            throw new ValidatorException();
        } else if (value instanceof float[] floatArray && floatArray.length < minLength) {
            throw new ValidatorException();
        } else if (value instanceof char[] charArray && charArray.length < minLength) {
            throw new ValidatorException();
        } else if (value instanceof byte[] byteArray && byteArray.length < minLength) {
            throw new ValidatorException();
        } else if (value instanceof short[] shortArray && shortArray.length < minLength) {
            throw new ValidatorException();
        } else if (value instanceof boolean[] booleanArray && booleanArray.length < minLength) {
            throw new ValidatorException();
        } else if (value instanceof Map<?, ?> map && map.size() < minLength) {
            throw new ValidatorException();
        }
    }
}
