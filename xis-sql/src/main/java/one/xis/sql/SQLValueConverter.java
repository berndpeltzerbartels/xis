package one.xis.sql;

import com.google.gson.Gson;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.Month;
import java.time.MonthDay;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

class SQLValueConverter {

    private final Gson gson = new Gson();

    Object toSqlValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof char[] chars) {
            return new String(chars);
        }
        if (value instanceof Character character) {
            return character.toString();
        }
        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        if (value instanceof UUID uuid) {
            return uuid.toString();
        }
        if (value instanceof Instant instant) {
            return Timestamp.from(instant);
        }
        if (value instanceof java.sql.Date || value instanceof java.sql.Time || value instanceof Timestamp) {
            return value;
        }
        if (value instanceof Date date) {
            return Timestamp.from(date.toInstant());
        }
        if (value instanceof Calendar calendar) {
            return Timestamp.from(calendar.toInstant());
        }
        if (value instanceof ZonedDateTime dateTime) {
            return dateTime.toOffsetDateTime();
        }
        if (value instanceof Year || value instanceof YearMonth || value instanceof MonthDay
                || value instanceof Duration || value instanceof Period) {
            return value.toString();
        }
        if (value instanceof Month month) {
            return month.name();
        }
        if (value instanceof DayOfWeek dayOfWeek) {
            return dayOfWeek.name();
        }
        if (!ROMapper.isSimpleType(value.getClass())) {
            return gson.toJson(value);
        }
        return value;
    }
}
