package one.xis.utils.temporal;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;

public class DateTimeUtils {

    public static LocalDateTime localDateTime(String datetime, Locale locale) {
        var pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.SHORT, FormatStyle.SHORT, IsoChronology.INSTANCE, locale);
        if (!pattern.contains("yyyy") && !pattern.contains("yyy") && pattern.contains("yy")) {
            pattern = pattern.replace("yy", "yyyy");
        }
        var dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(datetime, dateTimeFormatter);
    }

    public static ZonedDateTime zonedDateTimeByIso(String datetime, Locale locale, ZoneId zoneId) {
        var test = dateTimeFormatter(locale, zoneId).format(ZonedDateTime.now());
        return ZonedDateTime.parse(datetime, dateTimeFormatter(locale, zoneId));
    }

    static DateTimeFormatter dateTimeFormatter(Locale locale, ZoneId zoneId) {
        return dateTimeFormatterBuilder().toFormatter(locale).withZone(zoneId);
    }

    static DateTimeFormatterBuilder dateTimeFormatterBuilder() {
        return new DateTimeFormatterBuilder()
                .appendLocalized(FormatStyle.SHORT, FormatStyle.SHORT)
                .parseCaseInsensitive()
                .appendInstant();
    }
}
