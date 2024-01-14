package one.xis.parameter;

import com.google.gson.stream.JsonReader;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.FieldUtil;
import one.xis.validation.ValidatorResultElement;

import java.io.IOException;
import java.util.Objects;

@RequiredArgsConstructor
class ObjectDeserializer {
    private final FormattedParameterDeserializer parameterDeserializer;
    private final DeserializationErrorHandler deserializationErrorHandler;

    Object deserializeObject(JsonReader reader, Target target, ValidatorResultElement resultElement) throws IOException {
        var o = ClassUtils.newInstance(target.getType());
        readObjectFields(reader, o, resultElement);
        return o;
    }

    private void readObjectFields(JsonReader reader, Object o, ValidatorResultElement parentResult) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            var name = reader.nextName();
            var field = FieldUtil.getField(o.getClass(), name);
            if (field != null) {
                var result = parentResult.childElement(name, 0);
                var targetField = new TargetField(field);
                parameterDeserializer.read(reader, targetField, result).ifPresent(v -> {
                    try {
                        FieldUtil.setFieldValue(o, field, v);
                    } catch (Exception e) {
                        deserializationErrorHandler.injectionFailed(new TargetField(field), Objects.toString(v), result);
                    }
                });

            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }
}
