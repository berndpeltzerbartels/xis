package one.xis.widget;

import one.xis.context.TestContext;
import one.xis.resource.ReloadableResourceFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WidgetsTest {

    private Widgets widgets;
    private WidgetCompiler widgetCompiler;
    private WidgetFactory widgetFactory;
    private TestContext testContext;
    private ReloadableResourceFile resourceFile;

    private static final String WIDGET_ID = TestController.class.getName();

    @one.xis.Widget
    static class TestController {

    }


    @BeforeEach
    void init() {
        widgetFactory = mock(WidgetFactory.class);
        widgetCompiler = mock(WidgetCompiler.class);
        resourceFile = mock(ReloadableResourceFile.class);

        Widget widget = mock(Widget.class);
        when(widget.getId()).thenReturn(WIDGET_ID);

        when(resourceFile.getContent()).thenReturn("");
        when(widgetFactory.createWidget(any())).thenReturn(widget);


        testContext = TestContext.builder()//
                .withSingleton(Widgets.class)//
                .withSingleton(TestController.class)//
                .withMockedSingleton(widgetFactory)//
                .withMockedSingletons(widgetCompiler)//
                .build();

        widgets = testContext.getSingleton(Widgets.class);
    }

    @Nested
    class GetActualWidget {

        @BeforeEach
        void init() {
            when(resourceFile.isObsolete()).thenReturn(false);
        }

        @Test
        void getWidget() {
            Widget widget = widgets.getWidget(WIDGET_ID);

            assertThat(widget).isNotNull();
            verify(widgetCompiler, times(1)).compile(any());
            verify(widgetFactory, times(1)).createWidget(any());

        }
    }


    @Nested
    class DoNotRecreateWidget {
        @BeforeEach
        void init() {

        }

        @Test
        void getWidget() {

        }
    }


    @Nested
    class ReloadWidget {

        @BeforeEach
        void init() {


        }

        @Test
        void getWidget() {

        }
    }
}
