package one.xis.http;

import one.xis.context.DefaultComponent;

import java.util.Optional;

@DefaultComponent
public class NoOpHttpFrontendHandler implements HttpFrontendHandler {

    @Override
    public Optional<String> getRootPageHtml(String path) {
        return Optional.empty();
    }
}
