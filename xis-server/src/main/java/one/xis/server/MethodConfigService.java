package one.xis.server;


import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

import java.util.Collection;
import java.util.HashSet;

@XISComponent
@RequiredArgsConstructor
class MethodConfigService {

    private final Collection<Object> widgetControllers = new HashSet<>();
    private final Collection<Object> pageControllers = new HashSet<>();

    public void addPageController(Object controller) {

    }

    public void addWidgetController(Object controller) {

    }
}
