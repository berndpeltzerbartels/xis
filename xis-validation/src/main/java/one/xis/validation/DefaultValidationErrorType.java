package one.xis.validation;

import lombok.Getter;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;


enum DefaultValidationErrorType implements ValidationErrorType {
    NOT_A_NUMBER(Number.class, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE),
    NOT_A_TIME(LocalTime.class, Time.class),
    NOT_A_YEAR(LocalTime.class),
    NOT_A_MONTH(YearMonth.class),
    NOT_A_DATE(LocalDate.class),
    NOT_A_DATETIME(Date.class, Timestamp.class),
    NOT_AN_EMAIL();

    @Getter
    private final Set<Class<?>> fieldTypes;

    DefaultValidationErrorType(Class<?>... fieldTypes) {
        this.fieldTypes = new HashSet<>(Arrays.asList(fieldTypes));
    }

    DefaultValidationErrorType() {
        this.fieldTypes = new HashSet<>();
    }

    static Optional<DefaultValidationErrorType> errorForType(Class<?> c) {
        return Arrays.stream(values()).filter(errorType -> errorType.getFieldTypes().contains(c)).findFirst();
    }
}
