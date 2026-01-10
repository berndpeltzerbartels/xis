package one.xis.ws;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

class WSRequestHeaders {

    private final Map<WSRequestHeaderName, String> headers = new EnumMap<>(WSRequestHeaderName.class);
    private long lastModifiedEpochMilli;

    void setLastModified(Instant instant) {
        headers.put(WSRequestHeaderName.LAST_MODIFIED, formatDate(instant));
    }

    void setAccessToken(String token) {
        headers.put(WSRequestHeaderName.ACCESS_TOKEN, token);
    }

    void setRefreshToken(String token) {
        headers.put(WSRequestHeaderName.REFRESH_TOKEN, token);
    }

    void setMessageId(String messageId) {
        headers.put(WSRequestHeaderName.MESSAGE_ID, messageId);
    }

    String getHeader(WSRequestHeaderName headerName) {
        return headers.get(headerName);
    }

    String getLastModified() {
        return headers.get(WSRequestHeaderName.LAST_MODIFIED);
    }

    String getAccessToken() {
        return headers.get(WSRequestHeaderName.ACCESS_TOKEN);
    }

    String getRefreshToken() {
        return headers.get(WSRequestHeaderName.REFRESH_TOKEN);
    }

    String getMessageId() {
        return headers.get(WSRequestHeaderName.MESSAGE_ID);
    }

    long getLastModifiedAsEpochMilli() {
        if (lastModifiedEpochMilli != 0) {
            return lastModifiedEpochMilli;
        }
        String lastModified = getLastModified();
        if (lastModified == null) {
            lastModifiedEpochMilli = -1L;
            return lastModifiedEpochMilli;
        }
        Instant instant = Instant.from(RFC_1123_DATE_TIME.parse(lastModified));
        lastModifiedEpochMilli = instant.toEpochMilli();
        return lastModifiedEpochMilli;
    }

    private String formatDate(Instant instant) {
        return RFC_1123_DATE_TIME.format(instant.atZone(java.time.ZoneId.of("GMT")));
    }
}
