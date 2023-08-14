package test.widget;

import one.xis.Model;
import one.xis.Page;

import java.util.List;

@Page("/widgetInLoopPage.html")
class WidgetInLoopPage {

    @Model("Numbers")
    List<Integer> numbers() {
        return List.of(1, 2, 3, 4, 5, 6, 7, 8);
    }
}
