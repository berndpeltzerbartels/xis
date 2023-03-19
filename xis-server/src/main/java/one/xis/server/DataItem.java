package one.xis.server;

import lombok.Value;

@Value
class DataItem {
    String key;
    Object value;
    long timestamp;
}
