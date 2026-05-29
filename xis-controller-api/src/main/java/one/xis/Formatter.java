package one.xis;

import java.time.ZoneId;
import java.util.Locale;

/**
 * Converts values between Java objects and their textual form in templates and
 * form submissions.
 *
 * <p>Implement this interface and reference the implementation with
 * {@link UseFormatter} when the default conversion is not precise enough, for
 * example for money, coordinates, or application-specific date formats.</p>
 *
 * @param <T> Java value type handled by this formatter
 */
@ImportInstances
public interface Formatter<T> {

    /**
     * Formats a Java value for display in the current user locale and time zone.
     */
    String format(T t, Locale locale, ZoneId zoneId);

    /**
     * Parses submitted text into a Java value using the current user locale and
     * time zone.
     */
    T parse(String s, Locale locale, ZoneId zoneId);
}
