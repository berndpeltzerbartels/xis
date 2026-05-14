package one.xis.auth;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.*;
import one.xis.auth.idp.ExternalIDPService;
import one.xis.auth.idp.ExternalIDPServices;
import one.xis.context.AppContext;
import one.xis.security.SecurityUtil;
import one.xis.server.ClientConfigService;

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
    private final ClientConfigService clientConfigService;

    @ModelData("externalIdpIds")
    Collection<String> getExternalIdpIds() {
        return externalIDPServices.getExternalIDPServices().stream()
                .map(ExternalIDPService::getProviderId)
                .toList();
    }

    @ModelData("externalIdpUrls")
    Map<String, String> getExternalIdpUrls(@QueryParameter("redirect_uri") @NullAllowed String postLoginRedirectUrl) {
        String redirectUrl = redirectUrl(postLoginRedirectUrl);
        return externalIDPServices.getExternalIDPServices().stream()
                .collect(Collectors.toMap(ExternalIDPService::getProviderId, service -> service.createLoginUrl(redirectUrl)));
    }

    @ModelData("displayLoginForm")
    boolean isDisplayLoginForm() {
        return appContext.getSingletons(UserInfoService.class).stream()
                .map(UserInfoService.class::cast)
                .anyMatch(c -> !(c instanceof UserServicePlaceholder) && c.supportsLocalLogin());
    }

    @FormData("login")
    LoginData createLoginFormData(@QueryParameter("redirect_uri") @NullAllowed String redirectUrl) {
        return new LoginData(null, null, StateParameter.create(redirectUrl(redirectUrl), "local"));
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

    private String redirectUrl(String redirectUrl) {
        if (redirectUrl != null && !redirectUrl.isBlank()) {
            return redirectUrl;
        }
        String welcomePageId = clientConfigService.getConfig().getWelcomePageId();
        return welcomePageId == null || welcomePageId.isBlank() ? "/" : welcomePageId;
    }

}
