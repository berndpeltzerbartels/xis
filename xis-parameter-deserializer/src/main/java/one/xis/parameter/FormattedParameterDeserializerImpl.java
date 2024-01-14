package one.xis.parameter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.gson.GsonFactory;
import one.xis.utils.lang.CollectionUtils;
import one.xis.validation.Validation;
import one.xis.validation.ValidatorResultElement;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Optional;

@XISComponent
@RequiredArgsConstructor
class FormattedParameterDeserializerImpl implements FormattedParameterDeserializer {

    private final Validation validation;
    private final GsonFactory gsonFactory;
    private final DeserializationErrorHandler deserializationErrorHandler;

    private ArrayDeserializer arrayDeserializer;
    private ObjectDeserializer objectDeserializer;
    private GsonDeserializer gsonDeserializer;

    @XISInit
    void init() {
        arrayDeserializer = new ArrayDeserializer(this);
        objectDeserializer = new ObjectDeserializer(this, deserializationErrorHandler);
        gsonDeserializer = new GsonDeserializer(gsonFactory.gson(), deserializationErrorHandler);
    }

    @Override
    public Optional<Object> deserialize(String json, Field field, ValidatorResultElement parameterResult) throws IOException {
        return deserialze(json, new TargetField(field), parameterResult);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Object> deserialize(String paramValue, Parameter parameter, ValidatorResultElement validatorResultElement) throws IOException {
        if (paramValue == null) {
            if (Collection.class.isAssignableFrom(parameter.getType())) {
                return Optional.of(CollectionUtils.emptyInstance((Class<Collection<?>>) parameter.getType()));
            }
            return Optional.empty();
        } else if (String.class.isAssignableFrom(parameter.getType())) {
            return Optional.of(paramValue);
        }
        return deserialze(paramValue, new TargetParameter(parameter), validatorResultElement);
    }

    private Optional<Object> deserialze(String json, Target target, ValidatorResultElement parameterResult) throws IOException {
        var reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return read(reader, target, parameterResult);
    }

    @Override
    public Optional<Object> read(JsonReader reader, Target target, ValidatorResultElement result) throws IOException {
        Optional<Object> value;
        if (reader.peek() == JsonToken.STRING || reader.peek() == JsonToken.NUMBER) {
            value = gsonDeserializer.read(target, reader, result);
        } else if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            if (Collection.class.isAssignableFrom(target.getType())) {
                value = Optional.of(arrayDeserializer.deserializeArrayToCollection(reader, target, result));
            } else if (target.getType().isArray()) {
                value = Optional.of(arrayDeserializer.deserializeArrayToArray(reader, target, result));
            } else {
                throw new IllegalStateException();
            }
        } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            value = Optional.of(objectDeserializer.deserializeObject(reader, target, result));
        } else {
            throw new IllegalStateException();
        }
        value.ifPresent(v -> validation.validateAssignedValue(target.getType(), v, result));
        return value;
    }
}
