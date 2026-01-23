package one.xis.ws;

import lombok.RequiredArgsConstructor;
import one.xis.auth.URLForbiddenException;
import one.xis.context.Component;

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
