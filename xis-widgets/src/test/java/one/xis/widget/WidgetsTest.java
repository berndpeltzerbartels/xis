package one.xis.widget;

import one.xis.resource.ReloadableResourceFile;
import one.xis.resource.ResourceFile;
import one.xis.resource.ResourceFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WidgetsTest {

    private Widgets widgets;
    private WidgetCompiler widgetCompiler;
    private ResourceFiles resourceFiles;
    private ResourceFile htmlResourceFile;

    private static final String WIDGET_CLASS = "xyz.Test";
    private static final String HTML_SRC = "Some HTML";
    private static final String JAVASCRIPT = "Some Javascript";
    private static final String NEWER_JAVASCRIPT = "Some newer Javascript";

    @BeforeEach
    void init() {
        widgetCompiler = mock(WidgetCompiler.class);
        resourceFiles = mock(ResourceFiles.class);
        widgets = new Widgets(widgetCompiler, resourceFiles);
    }

    @Nested
    class GetNewWidget {

        @BeforeEach
        void init() {
            htmlResourceFile = mock(ResourceFile.class);

            when(widgetCompiler.compile(eq(HTML_SRC))).thenReturn(JAVASCRIPT);
            when(resourceFiles.getByPath(eq("xyz/Test.html"))).thenReturn(htmlResourceFile);
            when(htmlResourceFile.getContent()).thenReturn(HTML_SRC);
        }

        @Test
        void getWidget() {
            Widget widget = widgets.getWidget(WIDGET_CLASS);

            assertThat(widget.getJavascript()).isEqualTo(JAVASCRIPT);
            assertThat(widget.getHtmlResourceFile()).isEqualTo(htmlResourceFile);
        }
    }


    @Nested
    class DoNotRecreateWidget {
        @BeforeEach
        void init() {
            htmlResourceFile = mock(ResourceFile.class);

            when(widgetCompiler.compile(eq(HTML_SRC))).thenReturn(JAVASCRIPT);
            when(resourceFiles.getByPath(eq("xyz/Test.html"))).thenReturn(htmlResourceFile);
            when(htmlResourceFile.getContent()).thenReturn(HTML_SRC);
        }

        @Test
        void getWidget() {
            Widget widget1 = widgets.getWidget(WIDGET_CLASS);
            Widget widget2 = widgets.getWidget(WIDGET_CLASS);

            assertThat(widget1 == widget2).isTrue(); // identical

            // do not recompile
            verify(widgetCompiler, times(1)).compile(eq(HTML_SRC));
            //do nor reload
            verify(resourceFiles, times(1)).getByPath(eq("xyz/Test.html"));
        }
    }


    @Nested
    class ReloadWidget {

        @BeforeEach
        void init() {
            htmlResourceFile = mock(ReloadableResourceFile.class);
            when(((ReloadableResourceFile) htmlResourceFile).isObsolete()).thenReturn(true);

            when(resourceFiles.getByPath(eq("xyz/Test.html"))).thenReturn(htmlResourceFile);
            when(htmlResourceFile.getContent()).thenReturn(HTML_SRC);
            when(widgetCompiler.compile(eq(HTML_SRC))).thenReturn(JAVASCRIPT);

            Widget widget = widgets.getWidget(WIDGET_CLASS); // creates old version of the widget
            assertThat(widget.getJavascript()).isEqualTo(JAVASCRIPT);

        }

        @Test
        void getWidget() {
            when(widgetCompiler.compile(eq(HTML_SRC))).thenReturn(NEWER_JAVASCRIPT);

            Widget widget = widgets.getWidget(WIDGET_CLASS);

            assertThat(widget.getJavascript()).isEqualTo(NEWER_JAVASCRIPT);
        }
    }
}
