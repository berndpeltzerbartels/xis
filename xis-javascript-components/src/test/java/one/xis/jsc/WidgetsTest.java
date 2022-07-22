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
    private WidgetJavascriptCompiler widgetJavascriptCompiler;
    private WidgetFactory widgetFactory;
    private ReloadableResourceFile resourceFile;

    @one.xis.Widget("xyz")
    static class TestWidget {

    }

    @BeforeEach
    void init() {


        ResourceFiles resourceFiles = mock(ResourceFiles.class);
        widgetFactory = new WidgetFactory(resourceFiles);
        widgetJavascriptCompiler = mock(WidgetJavascriptCompiler.class);
        resourceFile = mock(ReloadableResourceFile.class);

        when(resourceFiles.getByPath(any())).thenReturn(resourceFile);
        when(resourceFile.getContent()).thenReturn("");

        TestContext testContext = TestContext.builder()//
                .withSingleton(Widgets.class)//
                .withMockedSingleton(widgetFactory)//
                .withMockedSingletons(widgetJavascriptCompiler)//
                .build();

        widgets = testContext.getSingleton(Widgets.class);
        widgets.add(new TestWidget());
    }

    @Nested
    class GetActualWidget {

        @BeforeEach
        void init() {
            when(resourceFile.isObsolete()).thenReturn(false);
        }

        @Test
        void getWidget() {
            WidgetJavascript widgetJavascript = widgets.get("xyz");

            assertThat(widgetJavascript).isNotNull();
            verify(widgetJavascriptCompiler, times(1)).compile(anyString(), eq(resourceFile), anyString());
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
            WidgetJavascript widgetJavascript = widgets.get("xyz");

            assertThat(widgetJavascript).isNotNull();
            verify(widgetJavascriptCompiler, times(2)).compile(anyString(), eq(resourceFile), anyString());
        }
    }
}
