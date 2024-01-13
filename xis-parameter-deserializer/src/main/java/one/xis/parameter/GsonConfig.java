package one.xis.parameter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISBean;
import one.xis.context.XISComponent;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Locale;

@XISComponent
@RequiredArgsConstructor
class GsonConfig {

    @XISBean
    Gson gson() {
        return new GsonBuilder()
                .setLenient()
                .registerTypeAdapter(Date.class, new UtilDateDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
                .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
                .registerTypeAdapter(Double.class, new NumberAdapter())
                .registerTypeAdapter(Float.class, new NumberAdapter())
                .registerTypeAdapter(BigDecimal.class, new NumberAdapter())
                .create();
    }

    static DateTimeFormatter localDateTimeFormatter(Locale locale) {
        var dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, locale);
        var pattern = ((SimpleDateFormat) dateFormat).toLocalizedPattern();
        if (pattern.contains("yy") && !pattern.contains("yyyy")) {
            pattern = pattern.replace("yy", "yyyy"); // We do not want year expresses with just 2 digits
        }
        return DateTimeFormatter.ofPattern(pattern).localizedBy(locale);
    }


    static DateTimeFormatter localDateFormatter(Locale locale) {
        var dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        var pattern = ((SimpleDateFormat) dateFormat).toLocalizedPattern();
        if (pattern.contains("yy") && !pattern.contains("yyyy")) {
            pattern = pattern.replace("yy", "yyyy"); // We do not want year expresses with just 2 digits
        }
        return DateTimeFormatter.ofPattern(pattern).localizedBy(locale);
    }


    class NumberAdapter extends TypeAdapter<Number> {

        @Override
        public Number read(JsonReader in) throws IOException {
            try {
                return NumberFormat.getInstance(getLocale()).parse(in.nextString());
            } catch (ParseException e) {
                throw new ConnectException();
            }
        }

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            out.value(NumberFormat.getInstance(getLocale()).format(value));
        }

        private Locale getLocale() {
            return UserContext.getInstance().getLocale();
        }
    }


    abstract class DateTimeAdapterBase<T extends Temporal> extends TypeAdapter<T> {

        @Override
        public T read(JsonReader in) throws IOException {
            var value = in.nextString();
            try {
                return parseLocalized(value, getLocalFormatter(getLocale()));
            } catch (DateTimeParseException e) {
                try {
                    return parseIso(value);
                } catch (DateTimeParseException e2) {
                    throw new ConversionException(e2);
                }
            }
        }

        private Locale getLocale() {
            return UserContext.getInstance().getLocale();
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            writeFormatted(out, value, getLocalFormatter(getLocale()));
        }


        private void writeFormatted(JsonWriter out, T value, DateTimeFormatter dateTimeFormatter) throws IOException {
            out.value(dateTimeFormatter.format(value));
        }

        abstract T parseLocalized(String value, DateTimeFormatter dateTimeFormatter) throws IOException;

        abstract T parseIso(String value) throws IOException;

        abstract DateTimeFormatter getLocalFormatter(Locale locale);
    }


    class LocalDateAdapter extends DateTimeAdapterBase<LocalDate> {

        @Override
        public LocalDate parseLocalized(String value, DateTimeFormatter dateTimeFormatter) throws IOException {
            try {
                return LocalDate.parse(value, dateTimeFormatter);
            } catch (DateTimeParseException e) {
                throw new ConversionException(e);
            }
        }

        @Override
        public LocalDate parseIso(String value) throws IOException {
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException e) {
                throw new ConversionException(e);
            }
        }

        @Override
        DateTimeFormatter getLocalFormatter(Locale locale) {
            return localDateFormatter(locale);
        }
    }

    class LocalDateTimeAdapter extends DateTimeAdapterBase<LocalDateTime> {


        @Override
        public LocalDateTime parseLocalized(String value, DateTimeFormatter dateTimeFormatter) throws IOException {
            try {
                return LocalDateTime.parse(value, dateTimeFormatter);
            } catch (DateTimeParseException e) {
                throw new ConversionException(e);
            }
        }

        @Override
        public LocalDateTime parseIso(String value) throws IOException {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException e) {
                throw new ConversionException(e);
            }
        }

        @Override
        DateTimeFormatter getLocalFormatter(Locale locale) {
            return localDateTimeFormatter(locale);
        }


    }

    class ZonedDateTimeAdapter extends DateTimeAdapterBase<ZonedDateTime> {

        @Override
        public ZonedDateTime parseLocalized(String value, DateTimeFormatter dateTimeFormatter) throws IOException {
            try {
                return ZonedDateTime.parse(value, dateTimeFormatter);
            } catch (DateTimeParseException e) {
                throw new ConversionException(e);
            }
        }

        @Override
        public ZonedDateTime parseIso(String value) throws IOException {
            try {
                return ZonedDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new ConversionException(e);
            }
        }


        @Override
        DateTimeFormatter getLocalFormatter(Locale locale) {
            return localDateTimeFormatter(locale);
        }
    }

    class OffsetDateTimeAdapter extends DateTimeAdapterBase<OffsetDateTime> {

        @Override
        public OffsetDateTime parseLocalized(String value, DateTimeFormatter dateTimeFormatter) throws IOException {
            try {
                return OffsetDateTime.parse(value, dateTimeFormatter);
            } catch (DateTimeParseException e) {
                throw new ConversionException(e);
            }
        }

        @Override
        public OffsetDateTime parseIso(String value) throws IOException {
            try {
                return OffsetDateTime.parse(value);
            } catch (DateTimeParseException e) {
                throw new ConversionException(e);
            }
        }

        @Override
        DateTimeFormatter getLocalFormatter(Locale locale) {
            return localDateTimeFormatter(locale);
        }
    }

    class UtilDateDateTimeAdapter extends TypeAdapter<Date> {

        private final ZonedDateTimeAdapter zonedDateTimeAdapter;

        public UtilDateDateTimeAdapter() {
            this.zonedDateTimeAdapter = new ZonedDateTimeAdapter();
        }

        @Override
        public void write(JsonWriter out, Date value) throws IOException {
            throw new AbstractMethodError();
        }

        @Override
        public Date read(JsonReader in) throws IOException {
            ZonedDateTime zonedDateTime = zonedDateTimeAdapter.read(in);
            try {
                return Date.from(zonedDateTime.toInstant());
            } catch (DateTimeParseException e) {
                throw new ConversionException(e);

            }
        }
    }
}