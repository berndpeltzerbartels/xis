package one.xis.auth;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.context.AppContext;
import one.xis.context.Component;
import one.xis.validation.Validator;
import one.xis.validation.ValidatorException;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;


@Component
@RequiredArgsConstructor
class LoginValidator implements Validator<LoginData> {

    private final AppContext appContext;
    private final Collection<AdditionalLoginFactor> additionalLoginFactors;

    @Override
    public void validate(@NonNull LoginData login, @NonNull AnnotatedElement annotatedElement, @NonNull UserContext userContext) throws ValidatorException {
        var credentialService = appContext.getOptionalSingleton(LocalCredentialService.class)
                .orElseThrow(() -> new IllegalStateException("Local credentials are not configured"));
        if (!credentialService.validateCredentials(login.getUsername(), login.getPassword())) {
            throw new ValidatorException();
        }
        for (AdditionalLoginFactor factor : additionalLoginFactors) {
            if (factor.isRequired(login.getUsername()) && !factor.verify(login.getUsername(), login.additionalFactorValue(factor.fieldName()))) {
                throw new ValidatorException();
            }
        }
    }
}
