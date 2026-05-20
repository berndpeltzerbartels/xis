package one.xis.server;

import one.xis.Action;
import one.xis.Frontlet;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.UploadConfiguration;
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

    @Frontlet
    class TestFrontletController {
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
        new ControllerWrapperFactory(mock(MainDeserializer.class), mock(ControllerMethodResultMapper.class), mock(ControllerResultMapper.class), mock(UploadConfiguration.class)).createControllerWrapper("/page.html", controller, PageControllerWrapper.class);
    }

    @Test
    void noExceptionFrontlet() {
        var controller = new TestFrontletController();
        new ControllerWrapperFactory(mock(MainDeserializer.class), mock(ControllerMethodResultMapper.class), mock(ControllerResultMapper.class), mock(UploadConfiguration.class)).createControllerWrapper("TestFrontletController", controller, PageControllerWrapper.class);
    }


    @Test
    void createController() {
    }
}
