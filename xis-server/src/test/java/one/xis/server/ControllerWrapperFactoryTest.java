package one.xis.server;

import one.xis.Action;
import one.xis.Model;
import one.xis.Page;
import one.xis.Widget;
import org.junit.jupiter.api.Test;

class ControllerWrapperFactoryTest {


    @Page(value = "/page.html")
    class TestPageController {

        @Model("x")
        String getX() {
            return "x";
        }


        @Model("y")
        String getY() {
            return "y";
        }

        @Action("button1")
        Class<?> action1() {
            return null;
        }

        @Action("button2")
        Class<?> action2() {
            return null;
        }
    }

    @Widget
    class TestWidgetController {
        @Model("x")
        String getX() {
            return "x";
        }


        @Model("y")
        String getY() {
            return "y";
        }

        @Action("button1")
        Class<?> action1() {
            return null;
        }

        @Action("button2")
        Class<?> action2() {
            return null;
        }
    }

    @Test
    void noExceptionPage() {
        var controller = new TestPageController();
        new ControllerWrapperFactory().createController("/page.html", controller);
    }

    @Test
    void noExceptionWidget() {
        var controller = new TestWidgetController();
        new ControllerWrapperFactory().createController("TestWidgetController", controller);
    }


    @Test
    void createController() {
    }
}