package one.xis.validation;

import one.xis.context.XISComponent;

import java.lang.reflect.AnnotatedElement;

@XISComponent
class EMailValidator implements Validator<String> {

    @Override
    public void validate(String value, AnnotatedElement annotatedElement) throws ValidatorException {
        //TODO
    }
}
