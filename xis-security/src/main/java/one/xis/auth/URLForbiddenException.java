package one.xis.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class URLForbiddenException extends RuntimeException {
    private final String url;
}
