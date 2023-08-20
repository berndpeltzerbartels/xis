package one.xis.server;

import one.xis.LinkAction;
import one.xis.Model;
import one.xis.Page;
import one.xis.Widget;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

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

        @LinkAction("button1")
        Class<?> action1() {
            return null;
        }

        @LinkAction("button2")
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

        @LinkAction("button1")
        Class<?> action1() {
            return null;
        }

        @LinkAction("button2")
        Class<?> action2() {
            return null;
        }
    }

    @Test
    void noExceptionPage() {
        var controller = new TestPageController();
        new ControllerWrapperFactory(mock(ParameterDeserializer.class)).createControllerWrapper("/page.html", controller);
    }

    @Test
    void noExceptionWidget() {
        var controller = new TestWidgetController();
        new ControllerWrapperFactory(mock(ParameterDeserializer.class)).createControllerWrapper("TestWidgetController", controller);
    }


    @Test
    void createController() {
    }
}