package one.xis.parameter;

import one.xis.validation.ValidatorResultElement;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

class DateTimeDeserializerTest {

    private final DateTimeDeserializer zoneDateTimeDeserializer = new DateTimeDeserializer();

    @Test
    void deserializeZoneDateTimeIso() throws IOException {
        var target = new ClassTargetElement("", ZonedDateTime.class);
        var context = new ParameterDeserializationContext(mock(ValidatorResultElement.class), Locale.GERMANY, ZoneId.of("Europe/Berlin"));
        var result = zoneDateTimeDeserializer.deserialize("2007-08-31T16:47+10:00", target, context);
        var expectedUtc = ZonedDateTime.of(2007, 8, 31, 6, 47, 0, 0, ZoneId.of("UTC"));

        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(ZonedDateTime.class);
        var zonedDateTime = (ZonedDateTime) result.get();
        var resultUtc = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
        assertThat(resultUtc.compareTo(expectedUtc)).isEqualTo(0);
    }

    @Test
    void deserializeZoneDateTimeGermany() throws IOException {
        var target = new ClassTargetElement("", ZonedDateTime.class);
        var context = new ParameterDeserializationContext(mock(ValidatorResultElement.class), Locale.GERMANY, ZoneId.of("Europe/Berlin"));
        var result = zoneDateTimeDeserializer.deserialize("31.08.2007, 16:47", target, context);
        var expectedUtc = ZonedDateTime.of(2007, 8, 31, 14, 47, 0, 0, ZoneId.of("UTC"));

        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(ZonedDateTime.class);
        var zonedDateTime = (ZonedDateTime) result.get();
        var resultUtc = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
        assertThat(resultUtc.compareTo(expectedUtc)).isEqualTo(0);
    }

    @Test
    void deserializeDateGermany() throws IOException {
        var target = new ClassTargetElement("", Date.class);
        var context = new ParameterDeserializationContext(mock(ValidatorResultElement.class), Locale.GERMANY, ZoneId.of("Europe/Berlin"));
        var result = zoneDateTimeDeserializer.deserialize("31.08.2007, 16:47", target, context);
        var expectedUtc = ZonedDateTime.of(2007, 8, 31, 14, 47, 0, 0, ZoneId.of("UTC"));

        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(Date.class);
        var date = (Date) result.get();
        var resultUtc = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
        assertThat(resultUtc.compareTo(expectedUtc)).isEqualTo(0);
    }

    @Test
    void offsetDateTimeIso() throws IOException {
        var target = new ClassTargetElement("", OffsetDateTime.class);
        var context = new ParameterDeserializationContext(mock(ValidatorResultElement.class), Locale.GERMANY, ZoneId.of("Europe/Berlin"));
        var result = zoneDateTimeDeserializer.deserialize("2007-08-31T16:47+10:00", target, context);
        var expectedUtc = ZonedDateTime.of(2007, 8, 31, 6, 47, 0, 0, ZoneId.of("UTC"));

        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(OffsetDateTime.class);
        var offsetDateTime = (OffsetDateTime) result.get();
        var resultUtc = offsetDateTime.atZoneSameInstant(ZoneId.of("UTC"));
        assertThat(resultUtc.compareTo(expectedUtc)).isEqualTo(0);
    }
}