package one.xis.boot;

import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.http.HttpFrontendHandler;
import one.xis.server.FrontendService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class XISBootFrontendHandler implements HttpFrontendHandler {

    private final FrontendService frontendService;

    @Override
    public Optional<String> getRootPageHtml(String path) {
        return Optional.of(frontendService.getRootPageHtml());
    }
}
