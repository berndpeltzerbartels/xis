package one.xis.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class PageControllerEntry {
    private final ControllerWrapper wrapper;
    private final PageUrl pageUrl;
}
