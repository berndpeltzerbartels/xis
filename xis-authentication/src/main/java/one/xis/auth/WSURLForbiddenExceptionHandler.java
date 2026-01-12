package one.xis.auth;

import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.ws.WSClientRequest;
import one.xis.ws.WSExceptionHandler;
import one.xis.ws.WSServerResponse;

@Component
@RequiredArgsConstructor
class WSURLForbiddenExceptionHandler implements WSExceptionHandler<URLForbiddenException> {

    @Override
    public WSServerResponse handleException(WSClientRequest request, URLForbiddenException exception) {
        var response = new WSServerResponse(401);
        if (request != null) {
            response.setMessageId(request.getMessageId());
        }
        response.setBody(null);
        response.getHeaders().put("X-Auth-Error", exception.getMessage());
        return response;
    }
}
