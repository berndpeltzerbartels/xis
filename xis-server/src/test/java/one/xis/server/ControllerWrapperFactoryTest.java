package one.xis.server;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.Widget;
import one.xis.deserialize.MainDeserializer;
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
        new ControllerWrapperFactory(mock(MainDeserializer.class), mock(ControllerMethodResultMapper.class), mock(ControllerResultMapper.class)).createControllerWrapper("/page.html", controller, PageControllerWrapper.class);
    }

    @Test
    void noExceptionWidget() {
        var controller = new TestWidgetController();
        new ControllerWrapperFactory(mock(MainDeserializer.class), mock(ControllerMethodResultMapper.class), mock(ControllerResultMapper.class)).createControllerWrapper("TestWidgetController", controller, PageControllerWrapper.class);
    }


    @Test
    void createController() {
    }
}