package one.xis.deserialize;

public enum DeserializerPriority implements Comparable<DeserializerPriority> {
    FRAMEWORK_LOW,
    FRAMEWORK_HIGH,
    CUSTOM_LOW,
    CUSTOM_NORMAL,
    CUSTOM_HIGH
}
