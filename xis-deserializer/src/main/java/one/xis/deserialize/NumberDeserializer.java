package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.context.XISComponent;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;


@XISComponent
@RequiredArgsConstructor
class NumberDeserializer implements JsonDeserializer<Number> {

    @Override
    public boolean matches(JsonToken token, AnnotatedElement target) {
        var targetType = getType(target);
        return Number.class.isAssignableFrom(targetType)
                || targetType.equals(BigInteger.class)
                || targetType.equals(BigDecimal.class)
                || targetType.equals(Number.class)
                || (targetType.isPrimitive() && targetType != boolean.class && targetType != char.class);

    }

    @Override
    public Optional<Number> deserialize(JsonReader reader,
                                        String path,
                                        AnnotatedElement target,
                                        UserContext userContext,
                                        MainDeserializer mainDeserializer,
                                        PostProcessingResults results) throws IOException {
        try {
            if (reader.peek().equals(JsonToken.NUMBER)) {
                return Optional.of(parseNumber(reader.nextDouble(), target));
            }
            if (reader.peek().equals(JsonToken.STRING)) {
                return Optional.of(parseNumber(reader.nextString(), target));
            }
        } catch (Exception e) {
            throw new DeserializationException(e);
        }
        return Optional.empty();
    }

    @Override
    public DeserializerPriority getPriority() {
        return DeserializerPriority.FRAMEWORK_LOW;
    }

    private Number parseNumber(Number value, AnnotatedElement target) {
        var targetType = getType(target);
        if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
            return value.intValue();
        }
        if (targetType.equals(Double.class) || targetType.equals(double.class)) {
            return value.doubleValue();
        }
        if (targetType.equals(Long.class) || targetType.equals(long.class)) {
            return value.longValue();
        }
        if (targetType.equals(Float.class) || targetType.equals(float.class)) {
            return value.floatValue();
        }
        if (targetType.equals(Short.class) || targetType.equals(short.class)) {
            return value.shortValue();
        }
        if (targetType.equals(Byte.class) || targetType.equals(byte.class)) {
            return value.byteValue();
        }
        if (targetType.equals(BigInteger.class)) {
            return BigInteger.valueOf(value.longValue());
        }
        if (targetType.equals(BigDecimal.class)) {
            return BigDecimal.valueOf(value.doubleValue());
        }
        throw new IllegalArgumentException("Unsupported number type: " + targetType);
    }


    private Number parseNumber(String value, AnnotatedElement target) {
        var type = getType(target);
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(value);
        }
        if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.parseDouble(value);
        }
        if (type.equals(Long.class) || type.equals(long.class)) {
            return Long.parseLong(value);
        }
        if (type.equals(Float.class) || type.equals(float.class)) {
            return Float.parseFloat(value);
        }
        if (type.equals(Short.class) || type.equals(short.class)) {
            return Short.parseShort(value);
        }
        if (type.equals(Byte.class) || type.equals(byte.class)) {
            return Byte.parseByte(value);
        }
        if (type.equals(BigInteger.class)) {
            return new BigInteger(value);
        }
        if (type.equals(BigDecimal.class)) {
            return new BigDecimal(value);
        }
        throw new IllegalArgumentException("Unsupported number type: " + type);
    }


}
