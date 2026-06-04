package one.xis.totp;

import one.xis.Action;
import one.xis.DefaultHtmlFile;
import one.xis.FormData;
import one.xis.HtmlFile;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.Title;
import one.xis.UserContext;
import one.xis.auth.LocalCredentialService;
import one.xis.validation.ValidatorMessageResolver;

@Page("/totp-setup.html")
@HtmlFile("/totp-setup.html")
@DefaultHtmlFile("/default-totp-setup.html")
class TOTPSetupController {

    private final LocalCredentialService localCredentialService;
    private final TOTPEnrollmentService enrollmentService;
    private final ValidatorMessageResolver messageResolver;

    TOTPSetupController(LocalCredentialService localCredentialService,
                        TOTPEnrollmentService enrollmentService,
                        ValidatorMessageResolver messageResolver) {
        this.localCredentialService = localCredentialService;
        this.enrollmentService = enrollmentService;
        this.messageResolver = messageResolver;
    }

    @Title
    String title(UserContext userContext) {
        return message("totp.setup.title", userContext);
    }

    @FormData("totpSetup")
    TOTPSetupCredentials setupCredentials() {
        return new TOTPSetupCredentials();
    }

    @Action("setup")
    @ModelData("totpSetupResult")
    TOTPSetupResult setup(@FormData("totpSetup") TOTPSetupCredentials credentials,
                          UserContext userContext) {
        if (!localCredentialService.validateCredentials(credentials.getUsername(), credentials.getPassword())) {
            return TOTPSetupResult.error(message("totp.setup.invalidCredentials", userContext));
        }
        return TOTPSetupResult.qrCode(credentials.getUsername(), enrollmentService.qrCodeDataUrl(credentials.getUsername()));
    }

    @ModelData("totpSetupTitle")
    String setupTitle(UserContext userContext) {
        return message("totp.setup.title", userContext);
    }

    @ModelData("totpSetupIntro")
    String setupIntro(UserContext userContext) {
        return message("totp.setup.intro", userContext);
    }

    @ModelData("totpSetupUsernameLabel")
    String usernameLabel(UserContext userContext) {
        return message("totp.setup.username", userContext);
    }

    @ModelData("totpSetupPasswordLabel")
    String passwordLabel(UserContext userContext) {
        return message("totp.setup.password", userContext);
    }

    @ModelData("totpSetupButtonLabel")
    String buttonLabel(UserContext userContext) {
        return message("totp.setup.button", userContext);
    }

    @ModelData("totpSetupQrTitle")
    String qrTitle(UserContext userContext) {
        return message("totp.setup.qrTitle", userContext);
    }

    @ModelData("totpSetupQrInstructions")
    String qrInstructions(UserContext userContext) {
        return message("totp.setup.qrInstructions", userContext);
    }

    @ModelData("totpSetupLoginLink")
    String loginLink(UserContext userContext) {
        return message("totp.setup.loginLink", userContext);
    }

    @ModelData("totpSetupResult")
    TOTPSetupResult setupResult() {
        return new TOTPSetupResult();
    }

    private String message(String key, UserContext userContext) {
        return messageResolver.getMessage(key, userContext);
    }
}
