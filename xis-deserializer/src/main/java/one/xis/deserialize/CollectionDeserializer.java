package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import one.xis.UserContext;
import one.xis.context.XISComponent;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.ParameterUtil;
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
    public boolean matches(JsonToken token, AnnotatedElement target) {
        return Collection.class.isAssignableFrom(getType(target));
    }

    @Override
    public Optional<Collection> deserialize(JsonReader reader, String path, AnnotatedElement target, UserContext userContext, MainDeserializer mainDeserializer, Collection<ReportedError> failed) throws IOException {
        var collectionType = getType(target);
        var collection = createCollection(collectionType);
        var elementTarget = getTypeParameter(target);
        reader.beginArray();
        int index = 0;
        while (reader.hasNext()) {
            mainDeserializer.deserialize(reader, path(path, index++), elementTarget, userContext, failed)
                    .ifPresentOrElse(collection::add, () -> handleDeserializationError(collection, path, target, failed));
        }
        reader.endArray();
        checkMandatory(collection, target, failed, path);
        return Optional.of(collection);
    }

    @Override
    public DeserializerPriority getPriority() {
        return DeserializerPriority.FRAMEWORK_LOW;
    }

    private void checkMandatory(Collection<?> collection, AnnotatedElement target, Collection<ReportedError> failed, String path) {
        if (target.isAnnotationPresent(Mandatory.class) && collection.isEmpty()) {
            var context = new ReportedErrorContext(path, target, Mandatory.class, UserContext.getInstance());
            failed.add(new ReportedError(context, MISSING_MANDATORY_PROPERTY.getMessageKey(), MISSING_MANDATORY_PROPERTY.getGlobalMessageKey()));
        }
    }

    private void handleDeserializationError(Collection<?> values, String path, AnnotatedElement target, Collection<ReportedError> failed) {
        values.add(null);
        var context = new ReportedErrorContext(path, target, NoAnnotation.class, UserContext.getInstance());
        failed.add(new ReportedError(context, CONVERSION_ERROR.getMessageKey(), CONVERSION_ERROR.getGlobalMessageKey()));
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
