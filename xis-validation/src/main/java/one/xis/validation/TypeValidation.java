package one.xis.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.stream.Stream;

@RequiredArgsConstructor
@SuppressWarnings("unchecked")
class TypeValidation {
    private final Collection<TypeValidator<?>> validators;
    private final ValidationErrors errors;

    void validate(Parameter parameter, Object parameterValue) {
        new ValidationWalker(new TypeValidationVisitor(errors)).walk(parameter, parameterValue);
    }

    // TODO: Es werden auch Felder, deren Validierung bereist fehlgeschlagen ist, weiterhin validiert. Das sollte nicht passieren.
    @Getter
    @RequiredArgsConstructor
    class TypeValidationVisitor implements ValidationVisitor {
        private final ValidationErrors errors;

        @Override
        public void visitCollectionParameter(Parameter parameter, Collection<?> coll, ArrayPathElement pathElement) {
            collectionValidators(parameter).forEach(validator -> validator.validate((Collection<Object>) coll, new Errors(coll, errors, pathElement, validator)));
        }

        @Override
        public void visitArrayParameter(Parameter parameter, Object[] array, ArrayPathElement pathElement) {
            arrayValidators(parameter).forEach(validator -> validator.validate(array, new Errors(array, errors, pathElement, validator)));
        }

        @Override
        public void visitSingleParameter(Parameter parameter, Object value, DefaultPathElement pathElement) {
            singleObjectValidators(parameter).forEach(validator -> validator.validate(value, new Errors(value, errors, pathElement, validator)));
        }

        @Override
        public void visitCollectionField(Field field, Collection<?> coll, ArrayPathElement arrayPathElement) {
            collectionValidators(field).forEach(validator -> validator.validate((Collection<Object>) coll, new Errors(coll, errors, arrayPathElement, validator)));
        }

        @Override
        public void visitArrayField(Field field, Object[] array, ArrayPathElement arrayPathElement) {
            arrayValidators(field).forEach(validator -> validator.validate(array, new Errors(array, errors, arrayPathElement, validator)));
        }

        @Override
        public void visitSingleObjectField(Field field, Object value, DefaultPathElement objectPathElement) {
            singleObjectValidators(field).forEach(validator -> validator.validate(value, new Errors(value, errors, objectPathElement, validator)));
        }
    }

    private Stream<TypeValidator<Object>> singleObjectValidators(Parameter parameter) {
        return validators.stream()
                .filter(validator -> getValidatorTargetType(validator).isAssignableFrom(parameter.getType()))
                .map(validator -> (TypeValidator<Object>) validator);
    }

    private Stream<TypeValidator<Object>> singleObjectValidators(Field field) {
        return validators.stream()
                .filter(validator -> getValidatorTargetType(validator).isAssignableFrom(field.getType()))
                .map(validator -> (TypeValidator<Object>) validator);
    }

    private Stream<TypeValidator<Object[]>> arrayValidators(Parameter parameter) {
        return validators.stream()
                .filter(validator -> getValidatorTargetType(validator).isAssignableFrom(parameter.getType().getComponentType()))
                .map(validator -> (TypeValidator<Object[]>) validator);
    }

    private Stream<TypeValidator<Object[]>> arrayValidators(Field field) {
        return validators.stream()
                .filter(validator -> getValidatorTargetType(validator).isAssignableFrom(field.getType().getComponentType()))
                .map(validator -> (TypeValidator<Object[]>) validator);
    }

    private Stream<TypeValidator<Collection<Object>>> collectionValidators(Parameter parameter) {
        var typeParameter = ClassUtils.getGenericInterfacesTypeParameter(parameter.getType(), Collection.class, 0);
        return collectionValidators(typeParameter);
    }

    private Stream<TypeValidator<Collection<Object>>> collectionValidators(Field field) {
        var typeParameter = ClassUtils.getGenericInterfacesTypeParameter(field.getType(), Collection.class, 0);
        return collectionValidators(typeParameter);
    }

    @SuppressWarnings("unchecked")
    private Stream<TypeValidator<Collection<Object>>> collectionValidators(Class<?> elementType) {
        return validators.stream()
                .filter(validator -> getValidatorTargetType(validator).isAssignableFrom(elementType))
                .map(validator -> (TypeValidator<Collection<Object>>) validator);
    }


    private Class<?> getValidatorTargetType(TypeValidator<?> validator) {
        return ClassUtils.getGenericInterfacesTypeParameter(validator.getClass(), TypeValidator.class, 0);
    }

}
