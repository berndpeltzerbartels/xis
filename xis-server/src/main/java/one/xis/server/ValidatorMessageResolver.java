package one.xis.server;


import one.xis.context.XISComponent;

@XISComponent
class ValidatorMessageResolver {

    String resolveMessage(ValidationErrorType errorType, JsonDeserializer.Target target, Object value) {
        return "";
    }


    String resolveMessage(ValidationErrorType errorType, JsonDeserializer.Target target) {
        return "";
    }
}
