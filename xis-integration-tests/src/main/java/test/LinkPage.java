package test;

import lombok.RequiredArgsConstructor;
import one.xis.Model;
import one.xis.Page;

@Page("/link.html")
@RequiredArgsConstructor
class LinkPage {
    private final LinkPageService linkPageService;

    @Model("page")
    String getPageUri() {
        return linkPageService.getPageUri();
    }

    @Model("widget")
    String getWidgetId() {
        return linkPageService.getWidgetId();
    }
}
