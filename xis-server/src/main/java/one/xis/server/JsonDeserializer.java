package one.xis.server;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.context.XISComponent;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.CollectionUtils;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.ParameterUtil;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;


@XISComponent
@RequiredArgsConstructor
class JsonDeserializer {

    private final Validation validation;

    public Object deserialze(String json, Parameter parameter, ValidatorResultElement parameterResult) throws IOException {
        return deserialze(json, new TargetParameter(parameter), parameterResult);
    }

    public Object deserialze(String json, Field field, ValidatorResultElement parameterResult) throws IOException {
        return deserialze(json, new TargetField(field), parameterResult);
    }


    private Object deserialze(String json, Target target, ValidatorResultElement parameterResult) throws IOException {
        var reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return read(reader, target, parameterResult);
    }


    private Object read(JsonReader reader, Target target, ValidatorResultElement result) throws IOException {
        Object value;
        if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            var type = target.getType();
            if (type.isArray()) {
                value = jsonArrayToArray(reader, target, result);
            } else if (Collection.class.isAssignableFrom(type)) {
                value = jsonArrayToCollection(reader, target, result);
            } else {
                throw new IllegalArgumentException("unsupported type: " + target);
            }
        } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            value = readObject(reader, target, result);
        } else if (reader.peek() == JsonToken.NUMBER) {
            value = readNumber(reader, target.getType());
        } else if (reader.peek() == JsonToken.STRING) {
            value = readString(reader, target, result);
        } else {
            throw new IllegalStateException();
        }
        validation.validateAssignedValue(target, value, result);
        return value;
    }

    private Object[] jsonArrayToArray(JsonReader reader, Target target, ValidatorResultElement result) throws IOException {
        return jsonArrayToCollection(reader, target.getName(), List.class, target.getType().getComponentType(), result).toArray();
    }


    @SuppressWarnings("unchecked")
    <C extends Collection<?>> C jsonArrayToCollection(JsonReader reader, Target target, ValidatorResultElement result) throws IOException {
        return jsonArrayToCollection(reader, target.getName(), (Class<C>) target.getType(), target.getElementType(), result);
    }

    <C extends Collection<?>> C jsonArrayToCollection(JsonReader reader, String name, Class<C> collectionType, Class<?> elementType, ValidatorResultElement parentResult) throws IOException {
        var list = new ArrayList<>();
        reader.beginArray();
        int index = 0;
        while (reader.hasNext()) {
            var result = parentResult.childElement(name, index);
            CollectionUtils.resize(list, index + 1);
            list.set(index, readObject(reader, elementType, result));
            index++;
        }
        reader.endArray();
        return toCollectionOfType(list, collectionType);
    }

    private String readString(JsonReader reader, Target target, ValidatorResultElement validatorResultElement) throws IOException {
        var str = reader.nextString();
        validation.validateBeforeAssignment(target, str, validatorResultElement);
        return str;
    }

    @SuppressWarnings("depecation")
    private Object readNumber(JsonReader reader, Class<?> type) throws IOException {
        if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
            return reader.nextInt();
        }
        if (type.equals(Long.TYPE) || type.equals(Long.class)) {
            return reader.nextLong();
        }
        if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
            return reader.nextBoolean() ? 1 : 0;
        }
        if (type.equals(Float.TYPE) || type.equals(Float.class)) {
            return reader.nextDouble();
        }
        if (type.equals(Double.TYPE) || type.equals(Double.class)) {
            return reader.nextDouble();
        }
        if (type.equals(BigInteger.class)) {
            return BigInteger.valueOf(reader.nextLong());
        }
        if (type.equals(BigDecimal.class)) {
            return BigDecimal.valueOf(reader.nextLong());
        }
        if (type.equals(Date.class)) {
            return Date.parse(reader.nextString());
        }
        if (type.equals(String.class)) {
            return reader.nextString();
        }
        if (type.equals(Object.class)) { // Groovy
            return BigDecimal.valueOf(reader.nextDouble());
        }
        throw new UnsupportedOperationException("parameter-type " + type);
    }

    private Object readObject(JsonReader reader, Target field, ValidatorResultElement result) throws IOException {
        return readObject(reader, field.getType(), result);
    }

    private Object readObject(JsonReader reader, Class<?> type, ValidatorResultElement result) throws IOException {
        var o = ClassUtils.newInstance(type);
        readObjectFields(reader, o, result);
        return o;
    }

    private void readObjectFields(JsonReader reader, Object o, ValidatorResultElement parentResult) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            var name = reader.nextName();
            var field = FieldUtil.getField(o.getClass(), name);
            if (field != null) {
                var result = parentResult.childElement(name, 0);
                Object value = null;
                try {
                    var targetField = new TargetField(field);
                    value = read(reader, targetField, result);
                    validation.validateBeforeAssignment(targetField, value, result);
                    if (!result.hasError()) {
                        FieldUtil.setFieldValue(o, field, value);
                    }
                } catch (IllegalArgumentException e) {
                    validation.assignmentError(new TargetField(field), value, result);
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    @SuppressWarnings("unchecked")
    <C extends Collection<?>> C toCollectionOfType(ArrayList<?> list, Class<C> collType) {
        if (collType.isAssignableFrom(list.getClass())) {
            return (C) list;
        }
        if (collType.isAssignableFrom(Set.class)) {
            return (C) new HashSet<>(list);
        }
        if (collType.isAssignableFrom(HashSet.class)) {
            return (C) new HashSet<>(list);
        }
        var constructor = ClassUtils.getConstructor(collType, Collection.class);
        if (constructor != null && !Modifier.isAbstract(collType.getModifiers()) && !Modifier.isInterface(collType.getModifiers())) {
            try {
                return constructor.newInstance(list);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new UnsupportedOperationException("create instance of " + collType);
    }

    interface Target {
        String getName();

        Class<?> getType();

        Class<?> getElementType();
    }

    @Value
    static class TargetField implements Target {
        @Delegate(excludes = Exclusions.class)
        Field field;

        @Override
        public String getName() {
            return field.getName();
        }

        @Override
        public Class<?> getElementType() {
            if (Collection.class.isAssignableFrom(field.getType())) {
                return FieldUtil.getGenericTypeParameter(field);
            }
            if (field.getType().isArray()) {
                return field.getType().getComponentType();
            }
            return field.getType();
        }
    }

    @Value
    static class TargetParameter implements Target {

        @Delegate(excludes = Exclusions.class)
        Parameter parameter;

        @Override
        public String getName() {
            if (parameter.isAnnotationPresent(FormData.class)) {
                return parameter.getAnnotation(FormData.class).value();
            }
            if (parameter.isAnnotationPresent(ModelData.class)) {
                return parameter.getAnnotation(ModelData.class).value();
            }
            return parameter.getName();
        }

        @Override
        public Class<?> getElementType() {
            if (Collection.class.isAssignableFrom(parameter.getType())) {
                return ParameterUtil.getGenericTypeParameter(parameter);
            }
            if (parameter.getType().isArray()) {
                return parameter.getType().getComponentType();
            }
            return parameter.getType();
        }
    }

    interface Exclusions {
        String getName();
    }
}








