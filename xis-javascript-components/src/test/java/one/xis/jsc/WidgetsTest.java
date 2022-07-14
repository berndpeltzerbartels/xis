package one.xis.jsc;

import one.xis.context.TestContext;
import one.xis.resource.ReloadableResourceFile;
import one.xis.resource.ResourceFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WidgetsTest {

    private Widgets widgets;
    private WidgetCompiler widgetCompiler;
    private WidgetFactory widgetFactory;
    private ReloadableResourceFile resourceFile;

    private static final String WIDGET_ID = "xyz";


    @BeforeEach
    void init() {
        Object widgetController = new Object();

        ResourceFiles resourceFiles = mock(ResourceFiles.class);
        widgetFactory = new WidgetFactory(resourceFiles);
        widgetCompiler = mock(WidgetCompiler.class);
        resourceFile = mock(ReloadableResourceFile.class);

        when(resourceFiles.getByPath(any())).thenReturn(resourceFile);
        when(resourceFile.getContent()).thenReturn("");

        TestContext testContext = TestContext.builder()//
                .withSingleton(Widgets.class)//
                .withMockedSingleton(widgetFactory)//
                .withMockedSingletons(widgetCompiler)//
                .build();

        widgets = testContext.getSingleton(Widgets.class);
        widgets.add(WIDGET_ID, widgetController);
    }

    @Nested
    class GetActualWidget {

        @BeforeEach
        void init() {
            when(resourceFile.isObsolete()).thenReturn(false);
        }

        @Test
        void getWidget() {
            Widget widget = widgets.get(WIDGET_ID);

            assertThat(widget).isNotNull();
            verify(widgetCompiler, times(1)).compile(anyString(), eq(resourceFile), anyString());
        }
    }


    @Nested
    class RecompileWidget {

        @BeforeEach
        void init() {
            when(resourceFile.isObsolete()).thenReturn(true);
        }

        @Test
        void getWidget() {
            Widget widget = widgets.get(WIDGET_ID);

            assertThat(widget).isNotNull();
            verify(widgetCompiler, times(2)).compile(anyString(), eq(resourceFile), anyString());
        }
    }
}
