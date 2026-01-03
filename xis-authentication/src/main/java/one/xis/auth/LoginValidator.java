package one.xis.auth;

import lombok.RequiredArgsConstructor;
import one.xis.UserContext;
import one.xis.context.Component;
import one.xis.validation.Validator;
import one.xis.validation.ValidatorException;

import java.lang.reflect.AnnotatedElement;


@Component
@RequiredArgsConstructor
class LoginValidator implements Validator<LoginData> {

    private final UserInfoService<UserInfo> userInfoService;

    @Override
    public void validate(LoginData login, AnnotatedElement annotatedElement, UserContext userContext) throws ValidatorException {
        if (!userInfoService.validateCredentials(login.getUsername(), login.getPassword())) {
            throw new ValidatorException();
        }
    }
}
