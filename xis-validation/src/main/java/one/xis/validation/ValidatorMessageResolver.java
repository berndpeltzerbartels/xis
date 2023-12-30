package one.xis.validation;


import one.xis.context.XISComponent;

@XISComponent
public class ValidatorMessageResolver {

    String resolveMessage(ValidationErrorType errorType, Object value) {
        return "";
    }


    String resolveMessage(ValidationErrorType errorType) {
        return "";
    }
}
