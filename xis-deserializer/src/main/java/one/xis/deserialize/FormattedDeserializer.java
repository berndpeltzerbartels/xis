package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.RequiredArgsConstructor;
import one.xis.Format;
import one.xis.Formatter;
import one.xis.UserContext;
import one.xis.context.XISComponent;
import one.xis.utils.lang.ClassUtils;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Optional;

@XISComponent
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
class FormattedDeserializer implements JsonDeserializer<Object> {

    private final Collection<Formatter> formatters;

    @Override
    public boolean matches(JsonToken token, AnnotatedElement target) {
        return target.isAnnotationPresent(Format.class);
    }

    @Override
    public Optional<Object> deserialize(JsonReader reader, String path, AnnotatedElement target, UserContext userContext, MainDeserializer mainDeserializer, Collection<ReportedError> failed) throws IOException {
        if (reader.peek().equals(JsonToken.NULL)) {
            reader.nextNull();
            return Optional.empty();
        }
        var formatter = getFormatter(target);
        if (!reader.peek().equals(JsonToken.STRING)) {
            throw new IllegalArgumentException("Expected a string");
        }
        try {
            return Optional.of(formatter.parse(reader.nextString(), userContext.getLocale(), userContext.getZoneId()));
        } catch (Exception e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    public DeserializerPriority getPriority() {
        return DeserializerPriority.FRAMEWORK_HIGH;
    }

    private Formatter getFormatter(AnnotatedElement target) {
        var formatterClass = target.getAnnotation(Format.class).value();
        return formatters.stream()
                .filter(f -> f.getClass().equals(formatterClass))
                .findFirst()
                .orElseGet(() -> ClassUtils.newInstance(formatterClass));
    }
}
