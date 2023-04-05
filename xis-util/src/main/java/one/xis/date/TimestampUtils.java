package one.xis.date;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class TimestampUtils {

    public static Long toLong(Object rv) {
        if (rv == null) {
            return null;
        }
        if (rv instanceof Number) {
            return ((Number) rv).longValue();
        }
        if (rv instanceof LocalDateTime) {
            var ldt = (LocalDateTime) rv;
            return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        if (rv instanceof ZonedDateTime) {
            return ((ZonedDateTime) rv).toInstant().toEpochMilli();
        }
        if (rv instanceof OffsetDateTime) {
            return ((OffsetDateTime) rv).toInstant().toEpochMilli();
        }
        if (rv instanceof Timestamp) {
            return ((Timestamp) rv).toInstant().toEpochMilli();
        }
        if (rv instanceof Date) {
            return ((Date) rv).toInstant().toEpochMilli();
        }
        throw new IllegalStateException("not a suppported timestamp: " + rv.getClass());
    }
}

