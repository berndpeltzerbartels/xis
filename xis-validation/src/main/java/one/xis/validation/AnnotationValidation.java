package one.xis.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.context.AppContext;
import one.xis.context.XISComponent;
import one.xis.utils.lang.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@XISComponent
@RequiredArgsConstructor
class AnnotationValidation {
    private final AppContext appContext;

    void validate(Parameter parameter, Object paramegterValue, ValidationErrors errors) {
        new ValidationWalker(new AnnotationValidationVisitor(errors)).walk(parameter, paramegterValue);
    }

    @Getter
    @RequiredArgsConstructor
    class AnnotationValidationVisitor implements ValidationVisitor {
        private final ValidationErrors errors;

        @Override
        public void visitCollectionParameter(Parameter parameter, Collection<?> coll, ArrayPathElement pathElement) {
            validate(parameter, coll, validators(parameter), pathElement);
        }

        @Override
        public void visitArrayParameter(Parameter parameter, Object[] array, ArrayPathElement pathElement) {
            validate(parameter, array, validators(parameter), pathElement);
        }

        @Override
        public void visitSingleParameter(Parameter parameter, Object value, DefaultPathElement pathElement) {
            validate(parameter, value, validators(parameter), pathElement);
        }

        @Override
        public void visitCollectionField(Field field, Collection<?> coll, ArrayPathElement pathElement) {
            validate(field, coll, validators(field), pathElement);
        }

        @Override
        public void visitArrayField(Field field, Object[] array, ArrayPathElement pathElement) {
            validate(field, array, validators(field), pathElement);
        }

        @Override
        public void visitSingleObjectField(Field field, Object value, DefaultPathElement pathElement) {
            validate(field, value, validators(field), pathElement);
        }

        private void validate(AnnotatedElement annotatedElement, Object value, List<AnnotationValidator> validators, PathElement pathElement) {
            if (errors.hasError(pathElement.toString())) {
                return;
            }
            for (var validator : validators) {
                try {
                    validator.validate(annotatedElement, value);
                } catch (ValidatorException e) {
                    if (annotatedElement instanceof Field field) {
                        addFieldErrorMessages(validator, field, e, pathElement);
                    } else if (annotatedElement instanceof Parameter parameter) {
                        addParameterErrorMessages(validator, parameter, e, pathElement);
                    } else {
                        throw new IllegalStateException("Unsupported annotated element: " + annotatedElement);
                    }
                }
            }
        }

        private void addParameterErrorMessages(AnnotationValidator validator, Parameter parameter, ValidatorException e, PathElement pathElement) {
            var message = validator.createMessage(e.getMessageKey(), e.getParameters(), parameter, UserContext.getInstance());
            var globalMessage = validator.createGlobalMessage(e.getMessageKey(), e.getParameters(), parameter, UserContext.getInstance());
            addErrorMessages(message, globalMessage, pathElement);
        }

        private void addFieldErrorMessages(AnnotationValidator validator, Field field, ValidatorException e, PathElement pathElement) {
            var message = validator.createMessage(e.getMessageKey(), e.getParameters(), field, UserContext.getInstance());
            var globalMessage = validator.createGlobalMessage(e.getMessageKey(), e.getParameters(), field, UserContext.getInstance());
            addErrorMessages(message, globalMessage, pathElement);
        }

        private void addErrorMessages(String message, String globalMessage, PathElement pathElement) {
            if (message != null) {
                errors.addError(pathElement.getPath(), message);
            }
            if (globalMessage != null) {
                errors.addGlobalError(globalMessage);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<AnnotationValidator> validators(AnnotatedElement annotatedElement) {
        return validatorAnnotations(annotatedElement)
                .map(this::annotationValidatorClass)
                .map(this::annotationValidator)
                .sorted((v1, v2) -> Integer.compare(v2.priority(), v1.priority()))
                .toList();
    }

    private AnnotationValidator annotationValidator(Class<AnnotationValidator> c) {
        return appContext.getOptionalSingleton(c).orElseGet(() -> ClassUtils.newInstance(c));
    }

    @SuppressWarnings("unchecked")
    private Class<AnnotationValidator> annotationValidatorClass(Annotation annotation) {
        return (Class<AnnotationValidator>) annotation.annotationType().getAnnotation(ValidatorClass.class).value();
    }

    private Stream<? extends Annotation> validatorAnnotations(AnnotatedElement annotatedElement) {
        return Stream.of(annotatedElement.getAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(ValidatorClass.class));
    }
}
