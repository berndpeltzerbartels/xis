package one.xis.server;

import lombok.Value;

@Value
class DataItem {
    Object value;
    long timestamp;
}
