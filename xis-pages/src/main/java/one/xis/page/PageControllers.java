package one.xis.page;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

import java.util.HashMap;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
class PageControllers {

    private final Map<String, Object> controllers = new HashMap<>();

    void addController(Object pageController, PageMetaData metaData) {
        controllers.put(metaData.getJavascriptClassname(), pageController);
    }

    Object getPageController(@NonNull String jsClassname) {
        return controllers.get(jsClassname);
    }
}
