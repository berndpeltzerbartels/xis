package test.page.core;

import lombok.RequiredArgsConstructor;
import one.xis.ModelData;
import one.xis.Page;

@Page("/link.html")
@RequiredArgsConstructor
class LinkPage {
    private final LinkPageService linkPageService;

    @ModelData("page")
    String getPageUri() {
        return linkPageService.getPageUri();
    }

    @ModelData("widget")
    String getWidgetId() {
        return linkPageService.getWidgetId();
    }
}
