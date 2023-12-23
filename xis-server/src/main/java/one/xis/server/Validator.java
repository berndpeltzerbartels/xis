package one.xis.server;

public interface Validator<T> {

    void validate(T t);

    ValidationErrorType validationerrorType();

    Class<T> getApplicableType();

}
