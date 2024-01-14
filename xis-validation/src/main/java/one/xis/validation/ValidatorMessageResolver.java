package one.xis.validation;


import one.xis.context.XISComponent;

@XISComponent
public class ValidatorMessageResolver {

    public String resolveMessage(ValidationErrorType errorType, Class<?> targetType, String value) {
        return "";
    }


    String resolveMessage(ValidationErrorType errorType) {
        return "";
    }
}
