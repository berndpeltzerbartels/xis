package one.xis.auth;

import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.ws.WSExceptionHandler;
import one.xis.ws.WSServerResponse;

@Component
@RequiredArgsConstructor
class WSURLForbiddenExceptionHandler implements WSExceptionHandler<URLForbiddenException> {

    @Override
    public WSServerResponse handleException(URLForbiddenException exception) {
        var response = new WSServerResponse(401);
        response.setBody(null);
        response.getHeaders().put("X-Auth-Error", exception.getMessage());
        return response;
    }
}
