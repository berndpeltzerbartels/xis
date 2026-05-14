package one.xis.utils.temporal;

import org.junit.jupiter.api.Test;

import java.util.Locale;

class DateTimeUtilsTest {

    @Test
    void dateTimeFormatter() {
        //var result = DateTimeUtils.zonedDateTimeByIso("2024-01-02T16:56:15.0Z", Locale.GERMANY, ZoneId.of("Europe/Berlin"));
        var result = DateTimeUtils.localDateTime("01.02.2024, 16:56", Locale.GERMANY);
    }
}