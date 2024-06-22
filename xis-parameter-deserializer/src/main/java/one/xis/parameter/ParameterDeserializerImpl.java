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
import one.xis.validation.*;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;

@XISComponent
@RequiredArgsConstructor
class ParameterDeserializerImpl implements ParameterDeserializer {

    private final Gson gson;
    private final Collection<FieldFormat<?>> fieldFormats;

    @Override
    public Object deserialize(String json, Parameter parameter, ValidationErrors errors, UserContext userContext) throws IOException {
        var rootPathElement = new RootPathElement().addChild(ValidationUtil.getParameterName(parameter));
        var reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        PathElement pathElement;
        if (Collection.class.isAssignableFrom(parameter.getType()) || parameter.getType().isArray()) {
            pathElement = new ArrayPathElement(rootPathElement);
        } else {
            pathElement = new DefaultPathElement(rootPathElement);
        }
        return deserializeAny(reader, parameter.getParameterizedType(), pathElement, errors, userContext);
    }

    @SuppressWarnings("unchecked")
    private Object deserializeAny(@NonNull JsonReader reader,
                                  @NonNull Type type,
                                  @NonNull PathElement pathElement,
                                  @NonNull ValidationErrors errors,
                                  @NonNull UserContext userContext) throws IOException {
        try {
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
                return deserializeJsonArray(reader, type, (ArrayPathElement) pathElement, errors, userContext);
            } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                if (type instanceof Class<?> clazz) {
                    return deserializeObject(reader, ClassUtils.newInstance(clazz), (DefaultPathElement) pathElement, errors, userContext);
                } else {
                    throw new IllegalStateException();
                }
            } else {
                throw new IllegalStateException();
            }
        } catch (Exception e) {
            errors.addError(pathElement.getPath());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Object deserializeJsonArray(@NonNull JsonReader reader,
                                        @NonNull Type genericType,
                                        @NonNull ArrayPathElement arrayPathElement,
                                        @NonNull ValidationErrors errors,
                                        @NonNull UserContext userContext) throws IOException {
        if (genericType instanceof ParameterizedType parameterizedType && Collection.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
            var elementType = parameterizedType.getActualTypeArguments()[0];
            var collection = (Collection<Object>) CollectionUtils.emptyInstance((Class<? extends Collection<?>>) parameterizedType.getRawType());
            deserializeJsonArray(reader, collection, elementType, arrayPathElement, errors, userContext);
            return collection;
        } else if (genericType instanceof Class<?> clazz && clazz.isArray()) {
            var elementType = clazz.getComponentType();
            var list = new ArrayList<>();
            deserializeJsonArray(reader, list, elementType, arrayPathElement, errors, userContext);
            return list.toArray((Object[]) Array.newInstance(elementType, list.size()));
        } else {
            throw new IllegalStateException("unsupported type: " + genericType);
        }
    }

    private void deserializeJsonArray(@NonNull JsonReader reader,
                                      @NonNull Collection<Object> collection,
                                      @NonNull Type elementType,
                                      @NonNull ArrayPathElement arrayElement,
                                      @NonNull ValidationErrors errors,
                                      @NonNull UserContext userContext) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            var pathElement = arrayElement.addChild();
            try {
                var value = deserializeAny(reader, elementType, pathElement, errors, userContext);
                collection.add(value);
            } catch (JsonProcessingException | JsonSyntaxException e) {
                addError(errors, pathElement, elementType);
            } catch (Exception e) {
                reader.skipValue();
                addError(errors, pathElement, elementType);
            }
        }
        reader.endArray();
    }

    private Object deserializeObject(@NonNull JsonReader reader,
                                     @NonNull Object o,
                                     @NonNull DefaultPathElement parent,
                                     @NonNull ValidationErrors errors,
                                     @NonNull UserContext userContext) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            var name = reader.nextName();
            var pathElement = parent.addChild(name);
            var field = FieldUtil.getField(o.getClass(), name);
            if (field == null) {
                reader.skipValue();
            } else {
                Object value;
                try {
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
                        value = deserializeAnyField(reader, field, pathElement, errors, userContext);
                    }
                    FieldUtil.setFieldValue(o, field, value);
                } catch (JsonProcessingException | JsonSyntaxException e) {
                    addError(errors, pathElement, field);
                } catch (Exception e) {
                    reader.skipValue();
                    addError(errors, pathElement, field);
                }
            }
        }
        reader.endObject();
        return o;
    }

    private void addError(@NonNull ValidationErrors errors,
                          @NonNull PathElement pathElement,
                          @NonNull Field field) {
        addError(errors, pathElement, field.getType());
    }

    private void addError(@NonNull ValidationErrors errors,
                          @NonNull PathElement pathElement,
                          @NonNull Type type) {
        var errorKey = getErrorKey(type);
        var path = pathElement.getPath();
        errors.addError(path, errorKey);
        errors.addGlobalError(errorKey + ".global");
    }

    private Object deserializeAnyField(@NonNull JsonReader reader,
                                       @NonNull Field field,
                                       @NonNull PathElement pathElement,
                                       @NonNull ValidationErrors errors,
                                       @NonNull UserContext userContext) throws IOException {
        var type = field.getGenericType();
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
            return deserializeJsonArray(reader, type, (ArrayPathElement) pathElement, errors, userContext);
        } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            if (type instanceof Class<?> clazz) {
                return deserializeObject(reader, ClassUtils.newInstance(clazz), (DefaultPathElement) pathElement, errors, userContext);
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


    private ValidationFieldInjectionError createValidationError(PathElement pathElement, Throwable e, Object value) {
        return createValidationError(null, pathElement, e, value);
    }

    private ValidationFieldInjectionError createValidationError(Field field, PathElement pathElement, Throwable e, Object value) {
        var validationError = new ValidationFieldInjectionError();
        validationError.setPath(pathElement.getPath());
        validationError.setValue(value);
        validationError.setField(field);
        if (e instanceof JsonSyntaxException jsonSyntaxException) {
            validationError.setThrowable(jsonSyntaxException.getCause());
        } else {
            validationError.setThrowable(e);
        }
        return validationError;
    }


    private String readString(@NonNull JsonReader reader) throws IOException {
        return reader.nextString();
    }

    private FieldFormat<?> typeAdapter(Class<? extends FieldFormat<?>> typeAdapterClass) {
        return fieldFormats.stream()
                .filter(typeAdapterClass::isInstance)
                .map(fieldFormat -> (FieldFormat<?>) fieldFormat)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Type adapter not found in context: " + typeAdapterClass + ". Must be a component"));
    }

    private String getErrorKey(Type type) {
        if (type instanceof Class<?> clazz) {
            if (ClassUtils.isNumber(clazz)) {
                return "invalidNumber";
            }
            if (ClassUtils.isDate(clazz)) {
                return "invalidDate";
            }
        }
        return "invalidValue";
    }

}
