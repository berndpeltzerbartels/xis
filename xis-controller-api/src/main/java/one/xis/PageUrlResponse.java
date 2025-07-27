package one.xis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PageUrlResponse implements Response {
    private final String url;

    @Override
    public Class<?> getControllerClass() {
        return null;
    }
}
