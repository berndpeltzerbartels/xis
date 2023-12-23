package one.xis.server;

import lombok.Getter;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


enum DefaultValidationErrorType implements ValidationErrorType {
    NOT_A_NUMBER(Number.class),
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
}
