package one.xis.server;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import one.xis.context.XISComponent;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.FieldUtil;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;


@XISComponent
class ParameterDeserializer {

    public Object deserialze(String json, Parameter parameter) throws IOException {
        return evaluate(new JsonReader(new StringReader(json)), parameter);
    }

    public Object evaluate(JsonReader reader, Parameter parameter) throws IOException {
        if (parameter.getType().isArray()) {
            // TODO
            return null;
        } else if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            var type = parameter.getType();
            var parameterizedType = parameter.getParameterizedType();
            return evaluateArray(reader, type, parameterizedType);
        } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            return readValue(reader, parameter.getType());
        } else if (reader.peek() == JsonToken.NUMBER) {
            return readValue(reader, parameter.getType());
        } else if (reader.peek() == JsonToken.STRING) {
            return readValue(reader, parameter.getType());
        } else {
            throw new IllegalStateException();
        }
    }

    private void evaluateObject(JsonReader reader, Object o) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            var name = reader.nextName();
            var field = FieldUtil.getField(o.getClass(), name);
            if (field != null) {
                FieldUtil.setFieldValue(o, field, readValue(reader, field.getType()));
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    private Collection<?> evaluateArray(JsonReader reader, Class<?> type, Type elementType) throws IOException {
        if (elementType instanceof ParameterizedType) {
            return evaluateArray(reader, type, (ParameterizedType) elementType);
        } else {
            return evaluateArray(reader, type, (Class<?>) elementType);
        }
    }

    private Collection<?> evaluateArray(JsonReader reader, Class<?> type, ParameterizedType elementType) throws IOException {
        var list = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            list.add(readValue(reader, type, elementType));
        }
        reader.endArray();
        return toCollectionOfType(list, type);
    }

    private Collection<?> evaluateArray(JsonReader reader, Class<?> type, Class<?> elementType) throws IOException {
        var list = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            list.add(readValue(reader, elementType));
        }
        reader.endArray();
        return toCollectionOfType(list, type);
    }

    private Object readValue(JsonReader reader, Class<?> type) throws IOException {
        switch (reader.peek()) {
            case BEGIN_ARRAY:
                throw new IllegalStateException("Collections mus have type-parameters");
            case BEGIN_OBJECT:
                return readObject(reader, type);
            case NULL:
                reader.nextNull();
                return null;
            case STRING:
                return reader.nextString();
            case NUMBER:
                return readNumber(reader, type);
            default:
                reader.skipValue();
        }
        return null;
    }

    private Object readValue(JsonReader reader, Class<?> type, ParameterizedType parameterizedType) throws IOException {
        var elementType = parameterizedType.getActualTypeArguments()[0];
        switch (reader.peek()) {
            case BEGIN_ARRAY:
                if (elementType instanceof ParameterizedType) {
                    return readArray(reader, type, (ParameterizedType) elementType);
                }
                return readArray(reader, type, (Class<?>) elementType);
            case BEGIN_OBJECT:
                return readObject(reader, (Class<?>) elementType);
            case NULL:
                reader.nextNull();
                return null;
            case STRING:
                return reader.nextString();
            case NUMBER:
                return readNumber(reader, (Class<?>) elementType);
            default:
                reader.skipValue();
                return null;
        }

    }

    @SuppressWarnings("deprecation")
    private Object readNumber(JsonReader reader, Class<?> type) throws IOException {
        if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
            return reader.nextInt();
        }
        if (type.equals(Long.TYPE) || type.equals(Long.class)) {
            return reader.nextLong();
        }
        if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
            return reader.nextBoolean();
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
        throw new UnsupportedOperationException("parameter-type " + type);
    }

    private Object readArray(JsonReader reader, Class<?> type, ParameterizedType elementType) throws IOException {
        var list = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            list.add(readValue(reader, type, elementType));
        }
        reader.endArray();
        return toCollectionOfType(list, type);
    }

    private Object readArray(JsonReader reader, Class<?> type, Class<?> elementType) throws IOException {
        var list = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            list.add(readValue(reader, elementType));
        }
        reader.endArray();
        return toCollectionOfType(list, type);
    }

    private Object readObject(JsonReader reader, Class<?> type) throws IOException {
        var o = ClassUtils.newInstance(type);
        evaluateObject(reader, o);
        return o;
    }


    private Collection<?> toCollectionOfType(ArrayList<?> list, Class<?> collType) {
        if (collType.isAssignableFrom(list.getClass())) {
            return list;
        }
        if (collType.isAssignableFrom(Set.class)) {
            return new HashSet<>(list);
        }
        if (collType.isAssignableFrom(HashSet.class)) {
            return new HashSet<>(list);
        }
        var constructor = ClassUtils.getConstructor(collType, Collection.class);
        if (constructor != null && !Modifier.isAbstract(collType.getModifiers()) && !Modifier.isInterface(collType.getModifiers())) {
            try {
                return (Collection<?>) constructor.newInstance(list);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new UnsupportedOperationException("create instance of " + collType);
    }

}





