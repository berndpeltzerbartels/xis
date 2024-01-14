package one.xis.validation;

import lombok.Getter;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;


public enum DefaultValidationErrorType implements ValidationErrorType {
    ILLEGAL_UNKNOWN_REASON,
    // TODO Geldbetrag ohne Währunfgsangabe
    NOT_A_DECIMAL(Number.class, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE),
    NOT_A_NUMBER(Number.class, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE),
    NOT_A_TIME(LocalTime.class, Time.class),
    NOT_A_YEAR(LocalTime.class),
    NOT_A_MONTH(YearMonth.class),
    NOT_A_DATE(LocalDate.class),
    NOT_A_DATETIME(Date.class, Timestamp.class, LocalDateTime.class, ZonedDateTime.class, OffsetDateTime.class),
    NOT_AN_EMAIL(),
    NO_TYPE_ADAPTER();

    @Getter
    private final Set<Class<?>> fieldTypes;

    DefaultValidationErrorType(Class<?>... fieldTypes) {
        this.fieldTypes = new HashSet<>(Arrays.asList(fieldTypes));
    }

    DefaultValidationErrorType() {
        this.fieldTypes = new HashSet<>();
    }

    public static Optional<DefaultValidationErrorType> errorForType(Class<?> c) {
        return Arrays.stream(values()).filter(errorType -> errorType.getFieldTypes().contains(c)).findFirst();
    }
}
