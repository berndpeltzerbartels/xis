package one.xis.server;

import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

@SuperBuilder
class ModelTimestampMethod extends ControllerMethod {

    @Override
    @SneakyThrows
    Optional<Long> invoke(Request request, Object controller) {
        Object rv = method.invoke(controller, prepareArgs(request));
        if (rv == null) {
            return Optional.empty();
        }
        if (rv instanceof Number) {
            return Optional.of(((Number) rv).longValue());
        }
        if (rv instanceof LocalDateTime) {
            var ldt = (LocalDateTime) rv;
            return Optional.of(ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        if (rv instanceof ZonedDateTime) {
            return Optional.of(((ZonedDateTime) rv).toInstant().toEpochMilli());
        }
        if (rv instanceof OffsetDateTime) {
            return Optional.of(((OffsetDateTime) rv).toInstant().toEpochMilli());
        }
        if (rv instanceof Timestamp) {
            return Optional.of(((Timestamp) rv).toInstant().toEpochMilli());
        }
        if (rv instanceof Date) {
            return Optional.of(((Date) rv).toInstant().toEpochMilli());
        }
        throw new IllegalStateException("not a suppported timestamp: " + rv.getClass());
    }

    @Override
    InvocationType getInvocationType() {
        return InvocationType.ACTION;
    }
}
