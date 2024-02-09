package one.xis.i18n;

import java.time.ZoneId;
import java.util.Locale;

public interface Mapper<T> {

    Class<T> getType();

    String format(T value, Locale locale, ZoneId zoneId);

    T parse(String value, Locale locale, ZoneId zoneId);

    String errorMessage(String value, Locale locale, ZoneId zoneId);
}
