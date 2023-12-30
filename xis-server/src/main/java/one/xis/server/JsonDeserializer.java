package one.xis.server;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.context.XISComponent;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.CollectionUtils;
import one.xis.utils.lang.FieldUtil;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;


@XISComponent
@RequiredArgsConstructor
class JsonDeserializer {

    private final Validation validation;


    public Object deserialze(String json, Parameter parameter, ValidatorResultElement parameterResult, Locale locale, ZoneId zoneId) throws IOException {
        return deserialze(json, new TargetParameter(parameter), parameterResult, locale, zoneId);
    }

    public Object deserialze(String json, Field field, ValidatorResultElement parameterResult, Locale locale, ZoneId zoneId) throws IOException {
        return deserialze(json, new TargetField(field), parameterResult, locale, zoneId);
    }


    private Object deserialze(String json, Target target, ValidatorResultElement parameterResult, Locale locale, ZoneId zoneId) throws IOException {
        var reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return read(reader, target, parameterResult, locale, zoneId);
    }


    private Object read(JsonReader reader, Target target, ValidatorResultElement result, Locale locale, ZoneId zoneId) throws IOException {
        Object value;
        if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            var type = target.getType();
            if (type.isArray()) {
                value = jsonArrayToArray(reader, target, result, locale, zoneId);
            } else if (Collection.class.isAssignableFrom(type)) {
                value = jsonArrayToCollection(reader, target, result, locale, zoneId);
            } else {
                throw new IllegalArgumentException("unsupported type: " + target);
            }
        } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            value = readObject(reader, target, result, locale, zoneId);
        } else if (reader.peek() == JsonToken.NUMBER) {
            value = readLeafValue(reader, target, result, locale, zoneId);
        } else if (reader.peek() == JsonToken.STRING) {
            value = readLeafValue(reader, target, result, locale, zoneId);
        } else {
            throw new IllegalStateException();
        }
        validation.validateAssignedValue(target, value, result);
        return value;
    }

    private Object readLeafValue(JsonReader reader, Target target, ValidatorResultElement result, Locale locale, ZoneId zoneId) {
        try {
            return readLeafValue(reader, target.getType(), locale, zoneId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ConversionException e) {
            validation.assignmentError(target, e.getValue(), result);
            return null;
        }
    }

    private Object[] jsonArrayToArray(JsonReader reader, Target target, ValidatorResultElement parentResult, Locale locale, ZoneId zoneId) throws IOException {
        return jsonArrayToList(reader, target, parentResult, locale, zoneId).toArray();
    }


    @SuppressWarnings("unchecked")
    <C extends Collection<?>> C jsonArrayToCollection(JsonReader reader, Target target, ValidatorResultElement parentResult, Locale locale, ZoneId zoneId) throws IOException {
        var list = (List<Object>) jsonArrayToList(reader, target, parentResult, locale, zoneId);
        if (target.getType().isInstance(list)) {
            return (C) list;
        }
        return CollectionUtils.convertCollectionClass(list, (Class<C>) target.getType());
    }

    private List<?> jsonArrayToList(JsonReader reader, Target target, ValidatorResultElement parentResult, Locale locale, ZoneId zoneId) throws IOException {
        var list = new ArrayList<>();
        reader.beginArray();
        int index = 0;
        while (reader.hasNext()) {
            var result = parentResult.childElement(target.getName(), index++);
            Target elementTarget;
            if (target.getElementType() instanceof ParameterizedType parameterizedType) {
                elementTarget = new ParameterizedTargetElement(target.getName(), parameterizedType);
            } else if (target.getElementType() instanceof Class<?> clazz) {
                elementTarget = new ClassTargetElement(target.getName(), clazz);
            } else {
                throw new IllegalStateException();
            }
            list.add(read(reader, elementTarget, result, locale, zoneId));
        }
        reader.endArray();
        return list;
    }


    private Object readObject(JsonReader reader, Target target, ValidatorResultElement result, Locale locale, ZoneId zoneId) throws IOException {
        return readObject(reader, target.getType(), result, locale, zoneId);
    }

    private Object readObject(JsonReader reader, Class<?> type, ValidatorResultElement result, Locale locale, ZoneId zoneId) throws IOException {
        var o = ClassUtils.newInstance(type);
        readObjectFields(reader, o, result, locale, zoneId);
        return o;
    }

    private void readObjectFields(JsonReader reader, Object o, ValidatorResultElement parentResult, Locale locale, ZoneId zoneId) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            var name = reader.nextName();
            var field = FieldUtil.getField(o.getClass(), name);
            if (field != null) {
                var result = parentResult.childElement(name, 0);
                Object value = null;
                try {
                    var targetField = new TargetField(field);
                    value = read(reader, targetField, result, locale, zoneId);
                    validation.validateBeforeAssignment(targetField, value, result);
                    if (!result.hasError()) {
                        FieldUtil.setFieldValue(o, field, value);
                    }
                } catch (IllegalArgumentException e) {
                    validation.assignmentError(new TargetField(field), value, result);
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }


    private Object readLeafValue(JsonReader reader, Class<?> type, Locale locale, ZoneId zoneId) throws IOException, ConversionException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        if (type.equals(Object.class) && reader.peek() == JsonToken.NUMBER) {
            type = BigDecimal.class; // GRoovy
        }
        if (type.equals(Short.TYPE) || type.equals(Short.class)) {
            return readShort(reader, locale);
        }
        if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
            return readInt(reader, locale);
        }
        if (type.equals(Long.TYPE) || type.equals(Long.class)) {
            return readLong(reader, locale);
        }
        if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
            return reader.nextBoolean();
        }
        if (type.equals(Float.TYPE) || type.equals(Float.class)) {
            return readFloat(reader, locale);
        }
        if (type.equals(Double.TYPE) || type.equals(Double.class)) {
            return readDouble(reader, locale);
        }
        if (type.equals(BigInteger.class)) {
            return readBigInteger(reader, locale);
        }
        if (type.equals(BigDecimal.class)) {
            return readBigDecimal(reader, locale);
        }
        if (type.equals(Date.class)) {
            return readDate(reader, locale, zoneId);
        }
        if (type.equals(ZonedDateTime.class)) {
            return readZonedDateTime(reader, locale, zoneId);
        }
        if (type.equals(OffsetDateTime.class)) {
            return readOffsetDateTime(reader, locale, zoneId);
        }
        if (type.equals(LocalDate.class)) {
            return readLocalDate(reader, locale);
        }
        if (type.equals(LocalDateTime.class)) {
            return readLocalDateTime(reader, locale);
        }
        if (type.equals(Year.class)) {
            return readYear(reader);
        }
        if (type.equals(YearMonth.class)) {
            return readYearMonth(reader, locale);
        }

        if (type.equals(Month.class)) {
            return readMonth(reader, locale);
        }
        if (type.equals(String.class)) {
            return reader.nextString();
        }
        if (type.isEnum()) {
            return readEnumValue(reader, (Class<? extends Enum>) type);
        }

        throw new UnsupportedOperationException("parameter-type " + type);
    }

    private short readShort(JsonReader reader, Locale locale) throws ConversionException {
        return readNumber(reader, locale, Number::shortValue);
    }

    private int readInt(JsonReader reader, Locale locale) throws ConversionException {
        return readNumber(reader, locale, Number::intValue);
    }

    private long readLong(JsonReader reader, Locale locale) throws ConversionException {
        return readNumber(reader, locale, Number::longValue);
    }

    private float readFloat(JsonReader reader, Locale locale) throws ConversionException {
        return readNumber(reader, locale, Number::floatValue);
    }

    private double readDouble(JsonReader reader, Locale locale) throws ConversionException {
        return readNumber(reader, locale, Number::doubleValue);
    }

    private Object readEnumValue(JsonReader reader, Class<? extends Enum> type) throws ConversionException {
        try {
            if (reader.peek() == JsonToken.NUMBER) {
                var index = -1;
                try {
                    index = reader.nextInt();
                    var allValues = allEnumValues(type);
                    return allValues[index];
                } catch (Exception e) {
                    throw new ConversionException(e, index);
                }
            } else if (reader.peek() == JsonToken.STRING) {
                return Enum.valueOf(type, reader.nextString());
            } else {
                throw new IllegalStateException();
            }
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }

    private Enum<?>[] allEnumValues(Class<?> e) throws Exception {
        var values = e.getMethod("values");
        return (Enum<?>[]) values.invoke(null);
    }

    private BigDecimal readBigDecimal(JsonReader reader, Locale locale) throws ConversionException {
        DecimalFormat numberFormat = (DecimalFormat) NumberFormat.getInstance(locale);
        numberFormat.setParseIntegerOnly(false);
        String str = null;
        try {
            str = reader.nextString();
            return BigDecimal.valueOf(numberFormat.parse(str).doubleValue());
        } catch (ParseException | IOException e) {
            throw new ConversionException(e, str);
        }
    }

    private BigInteger readBigInteger(JsonReader reader, Locale locale) throws ConversionException {
        String str = null;
        try {
            str = reader.nextString();
            return new BigInteger(str);
        } catch (NumberFormatException | IOException e) {
            throw new ConversionException(e, str);
        }
    }

    private <N extends Number> N readNumber(JsonReader reader, Locale locale, Function<Number, N> numberFunction) throws ConversionException {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        String str = null;
        try {
            str = reader.nextString();
            Number number = numberFormat.parse(str);
            return numberFunction.apply(number);
        } catch (ParseException e) {
            throw new ConversionException(e, str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Date readDate(JsonReader reader, Locale locale, ZoneId zoneId) throws ConversionException {
        return toDate(readZonedDateTime(reader, locale, zoneId));
    }

    private OffsetDateTime readOffsetDateTime(JsonReader reader, Locale locale, ZoneId zoneId) throws ConversionException {
        var localDateTime = readLocalDateTime(reader, locale);
        var offset = zoneId.getRules().getOffset(localDateTime);
        return OffsetDateTime.of(localDateTime, offset);
    }

    private LocalDate readLocalDate(JsonReader reader, Locale locale) throws ConversionException {
        String str;
        try {
            str = reader.nextString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            return readIsoLocalDate(str);
        } catch (ConversionException e) {
            return readLocalizedLocalDate(str, locale);
        }
    }


    private LocalDate readIsoLocalDate(String value) throws ConversionException {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new ConversionException(value);
        }
    }


    private LocalDate readLocalizedLocalDate(String value, Locale locale) throws ConversionException {
        for (var style : List.of(DateFormat.SHORT, DateFormat.MEDIUM, DateFormat.LONG, DateFormat.FULL)) {
            try {
                return readLocalizedLocalDate(value, locale, style);
            } catch (ConversionException e) {
                // NOOP
            }
        }
        throw new ConversionException(value);
    }

    private LocalDate readLocalizedLocalDate(String value, Locale locale, int dateFormatStyle) throws
            ConversionException {
        var dateFormat = DateFormat.getDateInstance(dateFormatStyle, locale);
        var pattern = ((SimpleDateFormat) dateFormat).toLocalizedPattern().replace(".yy,", ".yyyy,");
        var formatter = DateTimeFormatter.ofPattern(pattern).localizedBy(locale);
        try {
            return LocalDate.parse(value, formatter);
        } catch (DateTimeParseException e) {
            throw new ConversionException(value);
        }
    }

    private ZonedDateTime readZonedDateTime(JsonReader reader, Locale locale, ZoneId zoneId) throws ConversionException {
        return readLocalDateTime(reader, locale).atZone(zoneId);
    }

    private Year readYear(JsonReader reader) throws ConversionException {
        String str = null;
        try {
            str = reader.nextString();
            return Year.parse(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (DateTimeParseException e) {
            throw new ConversionException(str);
        }
    }

    private Month readMonth(JsonReader reader, Locale locale) throws ConversionException {
        String value = null;
        try {
            value = reader.nextString();
        } catch (IOException e) {
            throw new ConversionException(e, value);
        }
        try {
            return Month.valueOf(Month.class, value);
        } catch (IllegalArgumentException e) {
            try {
                return Month.from(DateTimeFormatter.ofPattern("MMMM").localizedBy(locale).parse(value));
            } catch (DateTimeParseException ex) {
                throw new RuntimeException("", ex);
            }
        }
    }

    private YearMonth readYearMonth(JsonReader reader, Locale locale) throws ConversionException {
        String value;
        try {
            value = reader.nextString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            return YearMonth.parse(value);
        } catch (IllegalArgumentException e) {
            try {
                return YearMonth.from(DateTimeFormatter.ofPattern("MMMM").localizedBy(locale).parse(value));
            } catch (DateTimeParseException ex) {
                throw new RuntimeException("", ex);
            }
        }
    }

    private LocalDateTime readLocalDateTime(JsonReader reader, Locale locale) throws ConversionException {
        String value;
        try {
            value = reader.nextString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            return readIsoLocalDateTime(value);
        } catch (ConversionException e) {
            return readLocalizedLocalDateTime(value, locale);
        }
    }

    private LocalDateTime readIsoLocalDateTime(String value) throws ConversionException {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new ConversionException(e, value);
        }
    }

    private LocalDateTime readLocalizedLocalDateTime(String value, Locale locale) throws ConversionException {
        var styles = List.of(DateFormat.SHORT, DateFormat.MEDIUM, DateFormat.LONG, DateFormat.FULL);
        for (var dateStyle : styles) {
            for (var timeStyle : styles) {
                try {
                    return readLocalizedLocalDateTime(value, locale, dateStyle, timeStyle);
                } catch (ConversionException e) {
                    // NOOP
                }
            }
        }
        throw new ConversionException(value);
    }

    private LocalDateTime readLocalizedLocalDateTime(String value, Locale locale, int dateStyle, int timeStyle) throws ConversionException {
        var dateFormat = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
        var pattern = ((SimpleDateFormat) dateFormat).toLocalizedPattern().replace(".yy,", ".yyyy,");
        var formatter = DateTimeFormatter.ofPattern(pattern).localizedBy(locale);
        try {
            return LocalDateTime.parse(value, formatter);
        } catch (DateTimeParseException e) {
            throw new ConversionException(value);
        }
    }

    private Date toDate(ZonedDateTime zonedDateTime) {
        return Date.from(zonedDateTime.toInstant());
    }

    interface Target {
        String getName();

        Class<?> getType();

        Type getElementType();


    }

    @Value
    static class ClassTargetElement implements Target {
        String name;
        Class<?> type;

        @Override
        public Class<?> getElementType() {
            return type;
        }

    }


    @Value
    static class ParameterizedTargetElement implements Target {
        String name;
        ParameterizedType type;

        @Override
        public Class<?> getType() {
            return (Class<?>) type.getRawType();
        }

        @Override
        public Type getElementType() {
            return type.getActualTypeArguments()[0];
        }

    }

    @Value
    static class TargetField implements Target {
        Field field;

        @Override
        public String getName() {
            return field.getName();
        }

        @Override
        public Class<?> getType() {
            return field.getType();
        }

        @Override
        public Type getElementType() {
            var type = field.getGenericType();
            if (type instanceof ParameterizedType elementType) {
                return elementType.getActualTypeArguments()[0];
            }
            if (type instanceof WildcardType wildcardType) {
                return wildcardType.getUpperBounds()[0];
            }
            return null;
        }

    }

    @Value
    static class TargetParameter implements Target {

        Parameter parameter;

        @Override
        public String getName() {
            if (parameter.isAnnotationPresent(FormData.class)) {
                return parameter.getAnnotation(FormData.class).value();
            }
            if (parameter.isAnnotationPresent(ModelData.class)) {
                return parameter.getAnnotation(ModelData.class).value();
            }
            return parameter.getName();
        }

        @Override
        public Class<?> getType() {
            return parameter.getType();
        }

        @Override
        public Type getElementType() {
            var type = parameter.getParameterizedType();
            if (type instanceof ParameterizedType elementType) {
                return elementType.getActualTypeArguments()[0];
            }
            if (type instanceof WildcardType wildcardType) {
                return wildcardType.getUpperBounds()[0];
            }
            return null;
        }


    }
}








