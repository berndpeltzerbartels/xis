package one.xis.auth;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.*;
import one.xis.auth.idp.ExternalIDPService;
import one.xis.auth.idp.ExternalIDPServices;
import one.xis.context.AppContext;
import one.xis.security.SecurityUtil;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
@Page("/login.html")
@HtmlFile("/login.html")
@DefaultHtmlFile("/default-login.html")
@RequiredArgsConstructor
class LoginFormController<U extends UserInfo> {

    private final UserInfoService<U> userInfoService;
    private final CodeStore codeStore;
    private final ExternalIDPServices externalIDPServices;
    private final AppContext appContext;

    @ModelData("externalIdpIds")
    Collection<String> getExternalIdpIds() {
        return externalIDPServices.getExternalIDPServices().stream()
                .map(ExternalIDPService::getProviderId)
                .toList();
    }

    @ModelData("externalIdpUrls")
    Map<String, String> getExternalIdpUrls(@URLParameter("redirect_uri") String postLoginRedirectUrl) { // Annotation korrigiert
        return externalIDPServices.getExternalIDPServices().stream()
                .collect(Collectors.toMap(ExternalIDPService::getProviderId, service -> service.createLoginUrl(postLoginRedirectUrl)));
    }

    @ModelData("displayLoginForm")
    boolean isDisplayLoginForm() {
        return appContext.getOptionalSingleton(UserInfoService.class)
                .filter(c -> !(c instanceof UserServicePlaceholder))
                .isPresent();
    }

    @FormData("login")
    LoginData createLoginFormData(@URLParameter("redirect_uri") @NonNull String redirectUrl) {
        return new LoginData(null, null, StateParameter.create(redirectUrl, "local"));
    }

    @Action("login")
    public LoginResponse login(@FormData("login") LoginData login) {
        if (userInfoService.validateCredentials(login.getUsername(), login.getPassword())) {
            var code = SecurityUtil.createRandomKey(12);
            codeStore.store(code, login.getUsername());
            return new LoginResponse(login.getState(), code);
        }
        throw new IllegalStateException("Invalid username or password"); // Already validated by LoginValidator
    }

}
