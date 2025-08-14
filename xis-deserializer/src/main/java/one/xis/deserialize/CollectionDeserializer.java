package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.NonNull;
import one.xis.UserContext;
import one.xis.context.XISComponent;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.ParameterUtil;
import one.xis.validation.AllElementsMandatory;
import one.xis.validation.Mandatory;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;

import static one.xis.deserialize.DefaultDeserializationErrorType.CONVERSION_ERROR;
import static one.xis.deserialize.DefaultDeserializationErrorType.MISSING_MANDATORY_PROPERTY;

@XISComponent
@SuppressWarnings("rawtypes")
class CollectionDeserializer implements JsonDeserializer<Collection> {

    @Override
    public boolean matches(@NonNull JsonToken token, @NonNull AnnotatedElement target) {
        return Collection.class.isAssignableFrom(getType(target));
    }

    @Override
    public Optional<Collection> deserialize(JsonReader reader,
                                            String path,
                                            AnnotatedElement target,
                                            UserContext userContext,
                                            MainDeserializer mainDeserializer,
                                            PostProcessingResults postProcessingResults) throws IOException {
        if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            return deserializeArray(reader, path, target, userContext, mainDeserializer, postProcessingResults);
        }
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            if (target.isAnnotationPresent(Mandatory.class)) {
                var context = new DeserializationContext(path, target, Mandatory.class, userContext);
                postProcessingResults.add(new InvalidValueError(context, MISSING_MANDATORY_PROPERTY.getMessageKey(), MISSING_MANDATORY_PROPERTY.getGlobalMessageKey(), null));
            }
            return Optional.of(createCollection(getType(target)));
        }
        var elementTarget = getTypeParameter(target);
        var value = mainDeserializer.deserialize(reader, path, elementTarget, userContext, postProcessingResults);
        if (value.isEmpty()) {
            return Optional.of(createCollection(getType(target)));
        }
        var collectionType = getType(target);
        var collection = createCollection(collectionType);
        collection.add(value);
        return Optional.of(collection);
    }


    private Optional<Collection> deserializeArray(JsonReader reader,
                                                  String path,
                                                  AnnotatedElement target,
                                                  UserContext userContext,
                                                  MainDeserializer mainDeserializer,
                                                  PostProcessingResults postProcessingResults) throws IOException {
        var collectionType = getType(target);
        var collection = createCollection(collectionType);
        var elementTarget = getTypeParameter(target);
        var deserialiaztionFailed = false;
        reader.beginArray();
        int index = 0;
        while (reader.hasNext()) {
            Optional<Object> result = Optional.empty();
            if (this.requiresFormatter(target)) {
                result = mainDeserializer.getDeserializer(FormattedDeserializer.class).deserialize(reader, path(path, index), target, userContext, mainDeserializer, postProcessingResults);
            } else {
                result = mainDeserializer.deserialize(reader, path(path, index), elementTarget, userContext, postProcessingResults).map(Object.class::cast);
            }
            if (result.isPresent()) {
                collection.add(result.get());
            } else {
                collection.add(null);
                if (elementTarget.isPrimitive() || target.isAnnotationPresent(AllElementsMandatory.class)) {
                    deserialiaztionFailed = true;
                }
            }
            index++;
        }
        if (deserialiaztionFailed) {
            handleDeserializationError(collection, path, target, postProcessingResults, userContext);
        }
        reader.endArray();
        checkMandatory(collection, target, postProcessingResults, path, userContext);
        return Optional.of(collection);
    }

    @Override
    public DeserializerPriority getPriority() {
        return DeserializerPriority.FRAMEWORK_HIGHEST;
    }

    private void checkMandatory(Collection<?> collection, AnnotatedElement target, PostProcessingResults postProcessingResults, String path, UserContext userContext) {
        if (target.isAnnotationPresent(Mandatory.class) && collection.isEmpty()) {
            var context = new DeserializationContext(path, target, Mandatory.class, userContext);
            postProcessingResults.add(new InvalidValueError(context, MISSING_MANDATORY_PROPERTY.getMessageKey(), MISSING_MANDATORY_PROPERTY.getGlobalMessageKey(), collection));
        }
    }

    private void handleDeserializationError(Collection<?> values, String path, AnnotatedElement target, PostProcessingResults postProcessingResults, UserContext userContext) {
        var context = new DeserializationContext(path, target, NoAnnotation.class, userContext);
        postProcessingResults.add(new InvalidValueError(context, CONVERSION_ERROR.getMessageKey(), CONVERSION_ERROR.getGlobalMessageKey(), values));
    }

    private String path(String parent, int index) {
        return String.format("%s[%d]", parent, index);
    }

    private Class<?> getTypeParameter(AnnotatedElement target) {
        if (target instanceof Field field) {
            return FieldUtil.getGenericTypeParameter(field);
        }
        if (target instanceof Parameter parameter) {
            return ParameterUtil.getGenericTypeParameter(parameter);
        }
        throw new IllegalArgumentException("Unsupported target type: " + target);
    }

    private Collection<Object> createCollection(Class<?> collectionType) {
        if (List.class.isAssignableFrom(collectionType)) {
            return new ArrayList<>();
        }
        if (Set.class.isAssignableFrom(collectionType)) {
            return new HashSet<>();
        }
        if (Queue.class.isAssignableFrom(collectionType)) {
            return new LinkedList<>();
        }
        if (Collection.class.isAssignableFrom(collectionType)) {
            return new ArrayList<>();
        }
        throw new IllegalArgumentException("Unsupported collection type: " + collectionType);
    }
}
