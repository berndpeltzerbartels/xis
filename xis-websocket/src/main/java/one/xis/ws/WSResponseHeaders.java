package one.xis.ws;

import lombok.Getter;

import java.time.Instant;
import java.util.EnumMap;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

@Getter
class WSResponseHeaders {
    private final EnumMap<WSResponseHeaderNames, String> headers = new EnumMap<>(WSResponseHeaderNames.class);

    public void setHeader(WSResponseHeaderNames name, String value) {
        headers.put(name, value);
    }

    public String getHeader(WSResponseHeaderNames name) {
        return headers.get(name);
    }

    public void setLastModified(long lastModified) {
        headers.put(WSResponseHeaderNames.LAST_MODIFIED, formatDate(lastModified));
    }

    private String formatDate(long epochMilli) {
        return formatDate(Instant.ofEpochMilli(epochMilli));
    }

    private String formatDate(Instant instant) {
        return RFC_1123_DATE_TIME.format(instant.atZone(java.time.ZoneId.of("GMT")));
    }
}
