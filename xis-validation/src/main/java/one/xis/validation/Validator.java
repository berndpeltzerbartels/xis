package one.xis.validation;

public interface Validator<T> {

    void validate(T t);

    ValidationErrorType validationerrorType();

    Class<T> getApplicableType();

}
