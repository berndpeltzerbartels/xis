package one.xis.http;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class MethodMatcher {
    private final HttpMethod method;
    private final Path path;


    MethodMatchResult matches(HttpMethod method, String requestPath) {
        if (this.method != method) {
            return MethodMatchResult.noMatch();
        }
        return this.path.matches(requestPath);
    }
}
