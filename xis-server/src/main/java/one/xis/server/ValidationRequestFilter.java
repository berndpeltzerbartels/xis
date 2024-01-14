package one.xis.server;

import one.xis.context.XISComponent;
import one.xis.validation.ValidationFailedException;

@XISComponent
class ValidationRequestFilter implements RequestFilter {

    @Override
    public void doFilter(ClientRequest request, ServerResponse response, RequestFilterChain filterChain) {
        try {
            filterChain.doFilter(request, response, filterChain);
        } catch (ValidationFailedException e) {
            filterChain.setServerResponse(handleValidationFailedException(e));
        }
    }

    private ServerResponse handleValidationFailedException(ValidationFailedException e) {
        var response = new ServerResponse();
        response.setHttpStatus(422);

        return null;
    }
}
