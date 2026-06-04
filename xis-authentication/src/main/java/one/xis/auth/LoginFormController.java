package one.xis.auth;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.*;
import one.xis.auth.idp.ExternalIDPService;
import one.xis.auth.idp.ExternalIDPServices;
import one.xis.context.AppContext;
import one.xis.security.SecurityUtil;
import one.xis.server.ClientConfigService;
import one.xis.validation.ValidatorMessageResolver;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
@Page("/login.html")
@HtmlFile("/login.html")
@DefaultHtmlFile("/default-login.html")
@RequiredArgsConstructor
class LoginFormController<U extends UserAccount> {

    private final CodeStore codeStore;
    private final ExternalIDPServices externalIDPServices;
    private final AppContext appContext;
    private final ClientConfigService clientConfigService;
    private final Collection<AdditionalLoginFactor> additionalLoginFactors;
    private final ValidatorMessageResolver messageResolver;

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
        return appContext.getOptionalSingleton(LocalCredentialService.class).isPresent();
    }

    @ModelData("totpLoginEnabled")
    boolean isTotpLoginEnabled() {
        return additionalLoginFactors.stream()
                .anyMatch(factor -> "totpCode".equals(factor.fieldName()));
    }

    @ModelData("loginFactorRegistrations")
    List<LoginFactorRegistrationLink> loginFactorRegistrations(UserContext userContext) {
        return additionalLoginFactors.stream()
                .map(AdditionalLoginFactor::registration)
                .flatMap(java.util.Optional::stream)
                .map(registration -> new LoginFactorRegistrationLink(
                        registration.url(),
                        messageResolver.getMessage(registration.messageKey(), userContext)))
                .toList();
    }

    @FormData("login")
    LoginData createLoginFormData(@QueryParameter("redirect_uri") @NullAllowed String redirectUrl) {
        return new LoginData(null, null, null, StateParameter.create(redirectUrl(redirectUrl), "local"));
    }

    @Action("login")
    public LoginResponse login(@FormData("login") LoginData login) {
        var credentialService = appContext.getOptionalSingleton(LocalCredentialService.class)
                .orElseThrow(() -> new IllegalStateException("Local credentials are not configured"));
        if (credentialService.validateCredentials(login.getUsername(), login.getPassword())) {
            var code = SecurityUtil.createRandomKey(12);
            codeStore.store(code, login.getUsername());
            return new LoginResponse(login.getState(), code);
        }
        throw new IllegalStateException("Invalid username or password"); // Already validated by LoginValidator
    }

    private String redirectUrl(String redirectUrl) {
        if (isSafeLocalRedirect(redirectUrl)) {
            return redirectUrl.trim();
        }
        String welcomePageId = clientConfigService.getConfig().getWelcomePageId();
        return welcomePageId == null || welcomePageId.isBlank() ? "/" : welcomePageId;
    }

    private boolean isSafeLocalRedirect(String redirectUrl) {
        if (redirectUrl == null || redirectUrl.isBlank()) {
            return false;
        }
        String value = redirectUrl.trim();
        return value.startsWith("/")
                && !value.startsWith("//")
                && value.chars().noneMatch(ch -> ch < 0x20 || ch == 0x7f);
    }

    record LoginFactorRegistrationLink(String url, String text) {
    }

}
