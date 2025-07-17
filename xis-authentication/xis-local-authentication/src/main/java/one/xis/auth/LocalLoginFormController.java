package one.xis.auth;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.*;
import one.xis.auth.idp.ExternalIDPService;
import one.xis.auth.token.StateParameter;
import one.xis.auth.token.TokenService;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static one.xis.auth.LocalLoginUrlProvider.LOGIN_URL;

@Setter
@Page(LOGIN_URL)
@HtmlFile("/login.html")
@DefaultHtmlFile("/default-login.html")
@RequiredArgsConstructor
class LocalLoginFormController<U extends UserInfo> {

    private final UserInfoService<U> userInfoService;
    private final TokenService tokenService;
    private final Collection<ExternalIDPService> externalIDPServices;

    @ModelData("externalIdpIds")
    Collection<String> getExternalIdpIds() {
        return externalIDPServices.stream()
                .map(ExternalIDPService::getProviderId)
                .toList();
    }

    @ModelData("externalIdpUrls")
    Map<String, String> getExternalIdpUrls(@URLParameter("redirect_uri") String postLoginRedirectUrl) { // Annotation korrigiert
        return externalIDPServices.stream()
                .collect(Collectors.toMap(ExternalIDPService::getProviderId, service -> service.createLoginUrl(postLoginRedirectUrl)));
    }

    @FormData("login")
    LocalLoginData createLoginFormData(@URLParameter("redirect_uri") @NonNull String redirectUrl) {
        return new LocalLoginData(null, null, StateParameter.create(redirectUrl));
    }

    @Action("login")
    public LocalLoginResponse login(@FormData("login") LocalLoginData login) {
        if (userInfoService.validateCredentials(login.getUsername(), login.getPassword())) {
            return new LocalLoginResponse(login.getState(), ""); // TODO code generation
        }
        throw new IllegalStateException("Invalid username or password");
    }

}
