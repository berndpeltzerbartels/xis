package one.xis.context;

public interface FieldHolder {

    default void fieldValueAssigned(FieldWrapper wrapper) {
        //noop
    }

}
