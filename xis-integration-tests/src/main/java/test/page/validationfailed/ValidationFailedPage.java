package test.page.validationfailed;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.validation.ValidationFailedException;

@Page("/validation-failed.html")
class ValidationFailedPage {

    @ModelData
    String value() {
        return "loaded";
    }

    @Action
    void reject() {
        throw new ValidationFailedException("invalid.credentials.global")
                .addFieldMessage("/form/name", "validation.invalid");
    }
}
