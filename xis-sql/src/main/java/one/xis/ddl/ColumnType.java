package one.xis.ddl;

public enum ColumnType {
    BIGINT(true, false),
    INTEGER(true, false),
    VARCHAR(false, true),
    TEXT(false, false),
    BOOLEAN(false, false),
    DATE(false, false),
    TIME(false, false),
    TIMESTAMP(false, false),
    DECIMAL(false, false);

    private final boolean integerNumber;
    private final boolean lengthRequired;

    ColumnType(boolean integerNumber, boolean lengthRequired) {
        this.integerNumber = integerNumber;
        this.lengthRequired = lengthRequired;
    }

    boolean isIntegerNumber() {
        return integerNumber;
    }

    boolean isLengthRequired() {
        return lengthRequired;
    }
}
