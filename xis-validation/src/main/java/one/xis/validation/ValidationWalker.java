package one.xis.validation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.FieldUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Collection;

import static one.xis.validation.ValidationUtil.getParameterName;

@RequiredArgsConstructor
public class ValidationWalker {
    private final ValidationVisitor visitor;

    public void walk(@NonNull Parameter parameter, @NonNull Object parameterValue) {
        var rootElement = new RootPathElement();
        if (parameterValue.getClass().isArray()) {
            var arrayElement = rootElement.addChild(new ArrayPathElement(rootElement));
            var arr = (Object[]) parameterValue;
            visitor.visitArrayParameter(parameter, arr, arrayElement);
            walkArray(arr, arrayElement);
        } else if (parameterValue instanceof Collection<?> coll) {
            var arrayElement = rootElement.addChild(new ArrayPathElement(rootElement));
            var arr = coll.toArray();
            visitor.visitArrayParameter(parameter, arr, arrayElement);
            walkArray(arr, arrayElement);
        } else {
            var pathElement = rootElement.addChild(getParameterName(parameter));
            visitor.visitSingleParameter(parameter, parameterValue, pathElement);
            walkFields(parameterValue, pathElement);
        }
    }


    private void walk(@NonNull Object parameterValue, @NonNull PathElement path) {
        if (parameterValue.getClass().isArray()) {
            walkArray((Object[]) parameterValue, path);
        } else if (parameterValue instanceof Collection) {
            walkArray((Collection<?>) parameterValue, path);
        } else {
            walkSingleObject(parameterValue, new RootPathElement());
        }
    }

    protected void walkArray(@NonNull Collection<?> o, @NonNull PathElement path) {
        walkArray(o.toArray(), path);
    }

    protected void walkSingleObject(@NonNull Object o, @NonNull PathElement objectPath) {
        walkFields(o, objectPath);
    }

    protected void walkArray(@NonNull Object[] o, @NonNull PathElement path) {
        for (int i = 0; i < o.length; i++) {
            walk(o[i], path);
        }
    }

    protected void walkFields(@NonNull Object o, @NonNull PathElement parentPath) {
        for (var field : FieldUtil.getAllFields(o.getClass())) {
            if (isIgnoreField(field)) {
                continue;
            }
            field.setAccessible(true);
            try {
                var value = field.get(o);
                if (value != null) {
                    walkFieldValue(field, value, (DefaultPathElement) parentPath);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void walkFieldValue(@NonNull Field field, @NonNull Object fieldValue, @NonNull DefaultPathElement parentPath) {
        var path = parentPath.addChild(field.getName());
        if (field.getType().isArray()) {
            walkArrayField(field, (Object[]) fieldValue, parentPath.addArrayChild());
        } else if (Collection.class.isAssignableFrom(field.getType())) {
            walkCollectionField(field, (Collection<?>) fieldValue, path);
        } else {
            walkSingleObjectField(field, fieldValue, path);
        }
    }

    protected void walkCollectionField(@NonNull Field collectionField, @NonNull Collection<?> o, @NonNull DefaultPathElement parentPath) {
        visitor.visitCollectionField(collectionField, o, parentPath.addArrayChild());
        walkArrayField(collectionField, o.toArray(), parentPath.addArrayChild());
    }

    protected void walkSingleObjectField(@NonNull Field field, @NonNull Object fieldValue, @NonNull DefaultPathElement parentPath) {
        visitor.visitSingleObjectField(field, fieldValue, parentPath);
        walkFields(fieldValue, parentPath);
    }

    protected void walkArrayField(@NonNull Field field, @NonNull Object[] array, @NonNull ArrayPathElement arrayElement) {
        visitor.visitArrayField(field, array, arrayElement);
        for (var o : array) {
            walkSingleObjectField(field, o, arrayElement.addChild());
        }
    }

    private boolean isIgnoreField(@NonNull Field field) {
        if (field.getType().isPrimitive()) {
            return true;
        }
        var packageName = field.getType().getPackageName();
        return packageName.startsWith("java.") || packageName.startsWith("javax.") || packageName.startsWith("com.sun.");
    }
}
