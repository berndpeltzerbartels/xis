package one.xis.parameter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.FieldFormat;
import one.xis.Format;
import one.xis.UserContext;
import one.xis.context.XISComponent;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.CollectionUtils;
import one.xis.utils.lang.FieldUtil;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
class Deserializer implements ParameterDeserializer {

    private final Gson gson;
    private final Collection<FieldFormat<?>> fieldFormats;

    @Override
    public Object deserialize(String json, Parameter parameter, Map<String, Throwable> errors, UserContext userContext) throws IOException {
        var reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return deserialize(reader, parameter.getParameterizedType(), parameter.getName(), new RootPathElement(), errors, userContext);
    }

    Object deserialize(String json, Type type, Map<String, Throwable> errors, UserContext userContext) throws IOException {
        var reader = new JsonReader(new StringReader(json));
        var path = new RootPathElement();
        reader.setLenient(true);
        if (type instanceof Class<?> clazz) {
            return deserializeObject(reader, ClassUtils.newInstance(clazz), path, errors, userContext);
        } else if (type instanceof ParameterizedType parameterizedType) {
            return deserializeArray(reader, parameterizedType, "", path, errors, userContext);
        } else {
            throw new IllegalStateException();
        }
    }

    private Object deserialize(@NonNull JsonReader reader,
                               @NonNull Field field,
                               @NonNull PathElement pathElement,
                               @NonNull Map<String, Throwable> errors,
                               @NonNull UserContext userContext) throws IOException {

        return deserialize(reader, field.getGenericType(), field.getName(), pathElement, errors, userContext);
    }

    @SuppressWarnings("unchecked")
    private Object deserialize(@NonNull JsonReader reader,
                               @NonNull Type type,
                               @NonNull String fieldName,
                               @NonNull PathElement pathElement,
                               @NonNull Map<String, Throwable> errors,
                               @NonNull UserContext userContext) throws IOException {

        if (type.equals(String.class)) {
            return readString(reader);
        } else if (isNumber(type)) {
            return readNumber(reader, (Class<? extends Number>) type);
        } else if (isBoolean(type)) {
            return readBoolean(reader);
        } else if (reader.peek() == JsonToken.NULL) {
            readNull(reader);
            return null;
        } else if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            return deserializeArray(reader, type, fieldName, pathElement, errors, userContext);
        } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            if (type instanceof Class<?> clazz) {
                return deserializeObject(reader, ClassUtils.newInstance(clazz), pathElement, errors, userContext);
            } else {
                throw new IllegalStateException();
            }
        } else {
            throw new IllegalStateException();
        }
    }


    private boolean isNumber(Type type) {
        if (type instanceof Class<?> clazz) {
            return Number.class.isAssignableFrom(clazz) || clazz.isPrimitive() && (type.equals(int.class) || type.equals(long.class) || type.equals(float.class) || type.equals(double.class) || type.equals(short.class) || type.equals(byte.class));
        }
        return false;
    }

    private boolean isBoolean(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz.equals(Boolean.class) || clazz.equals(boolean.class);
        }
        return false;
    }

    private void readNull(JsonReader reader) throws IOException {
        reader.nextNull();
    }

    private Object readBoolean(JsonReader reader) throws IOException {
        return gson.fromJson(reader.nextString(), Boolean.class);
    }

    private <N extends Number> N readNumber(JsonReader reader, Class<N> type) throws IOException {
        return gson.fromJson(reader.nextString(), type);
    }


    private Object deserializeObject(@NonNull JsonReader reader,
                                     @NonNull Object o,
                                     @NonNull PathElement parent,
                                     @NonNull Map<String, Throwable> errors,
                                     @NonNull UserContext userContext) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            var name = reader.nextName();
            var pathElement = parent.addChild(name);
            var field = FieldUtil.getField(o.getClass(), name);
            if (field == null) {
                reader.skipValue();
            } else {
                try {
                    Object value;
                    if (field.isAnnotationPresent(Format.class)) {
                        var format = field.getAnnotation(Format.class);
                        var adapterClass = format.value();
                        var adapter = typeAdapter(adapterClass);
                        var stringValue = readString(reader);
                        value = adapter.parse(stringValue, userContext.getLocale(), userContext.getZoneId());
                        if (!field.getType().isInstance(value)) {
                            throw new IllegalStateException(field + ": return value from adapter " + adapterClass.getSimpleName()
                                    + " is not compatible with field type: " + field.getType());
                        }
                    } else {
                        value = deserialize(reader, field, pathElement, errors, userContext);
                    }
                    FieldUtil.setFieldValue(o, field, value);
                } catch (JsonProcessingException | JsonSyntaxException e) {
                    errors.put(pathElement.toPathString(), e.getCause());
                } catch (Exception e) {
                    reader.skipValue();
                    errors.put(pathElement.toPathString(), e);
                }
            }
        }
        reader.endObject();
        return o;
    }

    @SuppressWarnings("unchecked")
    private Object deserializeArray(@NonNull JsonReader reader,
                                    @NonNull Type genericType,
                                    @NonNull String fieldName,
                                    @NonNull PathElement parent,
                                    @NonNull Map<String, Throwable> errors,
                                    @NonNull UserContext userContext) throws IOException {
        if (genericType instanceof ParameterizedType parameterizedType && Collection.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
            var elementType = assertNoArrayOrCollectionType(parameterizedType.getActualTypeArguments()[0]);
            var collection = (Collection<Object>) CollectionUtils.emptyInstance((Class<? extends Collection<?>>) parameterizedType.getRawType());
            deserializeArray(reader, collection, elementType, fieldName, parent, errors, userContext);
            return collection;
        } else if (genericType instanceof Class<?> clazz && clazz.isArray()) {
            var elementType = assertNoArrayOrCollectionType(clazz.getComponentType());
            var list = new ArrayList<>();
            deserializeArray(reader, list, elementType, fieldName, parent, errors, userContext);
            return list.toArray((Object[]) Array.newInstance(elementType, list.size()));
        } else {
            throw new IllegalStateException("unsupported type: " + genericType);
        }
    }

    private void deserializeArray(@NonNull JsonReader reader, Collection<Object> collection,
                                  @NonNull Type elementType,
                                  @NonNull String fieldName,
                                  @NonNull PathElement element,
                                  @NonNull Map<String, Throwable> errors,
                                  @NonNull UserContext userContext) throws IOException {
        var parent = element.getParent();
        parent.clearChildren();
        reader.beginArray();
        while (reader.hasNext()) {
            var pathElement = parent.addChild(fieldName);
            try {
                collection.add(deserialize(reader, elementType, fieldName, pathElement, errors, userContext));
            } catch (JsonProcessingException | JsonSyntaxException e) {
                errors.put(pathElement.toPathString(), e.getCause());
            } catch (Exception e) {
                reader.skipValue();
                errors.put(pathElement.toPathString(), e);
            }
        }
        reader.endArray();
    }


    private <T extends Type> T assertNoArrayOrCollectionType(T type) {
        if (type instanceof Class<?> clazz && (Collection.class.isAssignableFrom(clazz) || clazz.isArray())) {
            throw new IllegalStateException("array or collection type in array or collections is not allowed");
        }
        return type;
    }

    private String readString(JsonReader reader) throws IOException {
        return reader.nextString();
    }


    private FieldFormat<?> typeAdapter(Class<? extends FieldFormat<?>> typeAdapterClass) {
        return fieldFormats.stream()
                .filter(typeAdapterClass::isInstance)
                .map(fieldFormat -> (FieldFormat<?>) fieldFormat)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Type adapter not found in context: " + typeAdapterClass + ". Must be a component"));
    }

}
