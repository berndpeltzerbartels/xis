package one.xis;

import java.time.ZoneId;
import java.util.Locale;

public interface FieldFormat<T> {

    String format(T t, Locale locale, ZoneId zoneId);

    T parse(String s, Locale locale, ZoneId zoneId);
}
