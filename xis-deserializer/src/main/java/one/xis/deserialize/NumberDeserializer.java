package one.xis.deserialize;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.context.Component;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Optional;


@Component
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
        Object value = null;
        try {
            if (reader.peek().equals(JsonToken.NUMBER)) {
                value = reader.nextDouble();
                return Optional.of(parseNumber((Number) value, target));
            }
            if (reader.peek().equals(JsonToken.STRING)) {
                value = reader.nextString();
                return Optional.of(parseNumber((String) value, target, userContext.getLocale()));
            }
            if (reader.peek().equals(JsonToken.NULL)) {
                reader.nextNull();
                throw new DeserializationException("Null value encountered for character deserialization, expected a number.", null);
            }
            reader.skipValue();
            throw new DeserializationException("Expected a number or string for number deserialization, but found: ", reader.peek());
        } catch (Exception e) {
            if ("".equals(value)) {
                return Optional.empty();
            }
            throw new DeserializationException(e, value != null ? value.toString() : "");
        }
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


    private Number parseNumber(String value, AnnotatedElement target, Locale locale) {
        try {
            return parseCanonicalNumber(value, target);
        } catch (RuntimeException ignored) {
            return parseLocalizedNumber(value, target, locale);
        }
    }

    private Number parseCanonicalNumber(String value, AnnotatedElement target) {
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

    private Number parseLocalizedNumber(String value, AnnotatedElement target, Locale locale) {
        var parsed = localizedBigDecimal(value, locale);
        var type = getType(target);
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return integerValue(parsed).intValueExact();
        }
        if (type.equals(Double.class) || type.equals(double.class)) {
            return parsed.doubleValue();
        }
        if (type.equals(Long.class) || type.equals(long.class)) {
            return integerValue(parsed).longValueExact();
        }
        if (type.equals(Float.class) || type.equals(float.class)) {
            return parsed.floatValue();
        }
        if (type.equals(Short.class) || type.equals(short.class)) {
            return integerValue(parsed).shortValueExact();
        }
        if (type.equals(Byte.class) || type.equals(byte.class)) {
            return integerValue(parsed).byteValueExact();
        }
        if (type.equals(BigInteger.class)) {
            return integerValue(parsed).toBigIntegerExact();
        }
        if (type.equals(BigDecimal.class)) {
            return parsed;
        }
        throw new IllegalArgumentException("Unsupported number type: " + type);
    }

    private BigDecimal localizedBigDecimal(String value, Locale locale) {
        var text = value.trim();
        var format = NumberFormat.getNumberInstance(locale == null ? Locale.getDefault() : locale);
        if (format instanceof DecimalFormat decimalFormat) {
            decimalFormat.setParseBigDecimal(true);
        }
        var position = new ParsePosition(0);
        var parsed = format.parse(text, position);
        if (parsed == null || position.getIndex() != text.length()) {
            throw new IllegalArgumentException("Invalid localized number: " + value);
        }
        if (parsed instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (parsed instanceof BigInteger bigInteger) {
            return new BigDecimal(bigInteger);
        }
        return BigDecimal.valueOf(parsed.doubleValue());
    }

    private BigDecimal integerValue(BigDecimal value) {
        return value.stripTrailingZeros();
    }


}
