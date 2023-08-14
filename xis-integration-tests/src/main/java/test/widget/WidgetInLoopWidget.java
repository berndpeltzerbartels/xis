package test.widget;

import one.xis.Model;
import one.xis.Parameter;
import one.xis.Widget;

@Widget
class WidgetInLoopWidget {

    @Model("value")
    Integer value(@Parameter("number") int number) {
        return number;
    }

    @Model("square")
    Integer square(@Parameter("number") int number) {
        return number * number;
    }
}
