package one.xis.auth;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.validation.Validator;
import one.xis.validation.ValidatorException;

import java.lang.reflect.AnnotatedElement;


@XISComponent
@RequiredArgsConstructor
class LocalLoginValidator implements Validator<LocalLoginData> {

    private final UserInfoService<UserInfo> userInfoService;

    @Override
    public void validate(LocalLoginData login, AnnotatedElement annotatedElement) throws ValidatorException {
        var userInfo = userInfoService.getUserInfo(login.getUsername()).orElseThrow(ValidatorException::new);
        if (!userInfo.getPassword().equals(login.getPassword())) {
            throw new ValidatorException();
        }
    }
}
