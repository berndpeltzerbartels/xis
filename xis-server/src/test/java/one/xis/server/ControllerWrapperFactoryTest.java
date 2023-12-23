package one.xis.server;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.Widget;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class ControllerWrapperFactoryTest {


    @Page(value = "/page.html")
    class TestPageController {

        @ModelData("x")
        String getX() {
            return "x";
        }


        @ModelData("y")
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
        @ModelData("x")
        String getX() {
            return "x";
        }


        @ModelData("y")
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
        new ControllerWrapperFactory(mock(ParameterPreparation.class)).createControllerWrapper("/page.html", controller);
    }

    @Test
    void noExceptionWidget() {
        var controller = new TestWidgetController();
        new ControllerWrapperFactory(mock(ParameterPreparation.class)).createControllerWrapper("TestWidgetController", controller);
    }


    @Test
    void createController() {
    }
}