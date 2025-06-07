package one.xis.server;

import lombok.Setter;
import one.xis.security.AuthenticationService;
import one.xis.security.InvalidCredentialsException;
import one.xis.security.LocalAuthenticationProviderService;
import one.xis.security.Login;

@Setter
class LoginControllerWrapper extends PageControllerWrapper {

    private LocalAuthenticationProviderService localAuthenticationProviderService;
    private AuthenticationService authenticationService;

    void invokeGetLoginData(ClientRequest request, ControllerResult controllerResult) {
        controllerResult.getFormData().put("state", authenticationService.createStateParameter(targetUri(request)));
    }

    ApiTokens invokeLogin(ClientRequest request) throws InvalidCredentialsException {
        var username = request.getFormData().get("username");
        var password = request.getFormData().get("password");
        var state = request.getFormData().get("state");
        authenticationService.verifyState(state);
        var code = localAuthenticationProviderService.login(new Login(username, password, state));
        var tokens = authenticationService.requestTokens(code, state);
        return new ApiTokens(tokens.getAccessToken(), tokens.getExpiresIn(), tokens.getRefreshToken(), tokens.getRefreshExpiresIn());
    }

    private String targetUri(ClientRequest request) {
        var targetUri = request.getUrlParameters().get("targetUri");
        if (targetUri == null || targetUri.isBlank()) {
            return "/";
        }
        checkTargetUri(targetUri);
        return targetUri;
    }

    private void checkTargetUri(String targetUri) {
        if (targetUri.startsWith("http://") || targetUri.startsWith("https://")) {
            throw new RuntimeException("Invalid target URI: " + targetUri);
        }
    }

}
