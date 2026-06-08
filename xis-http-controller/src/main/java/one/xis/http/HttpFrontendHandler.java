package one.xis.http;

import java.util.Optional;

public interface HttpFrontendHandler {

    Optional<String> getRootPageHtml(String path);
}
