package one.xis.server;

import one.xis.DefaultHtmlFile;
import one.xis.HtmlFile;
import one.xis.resource.Resources;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HtmlResourcePathResolverTest {

    @Test
    void noAnnotation() {
        class Controller {
        }
        Resources resources = mock(Resources.class);
        HtmlResourcePathResolver resolver = new HtmlResourcePathResolver(resources);

        String expected = "one/xis/server/Controller.html";
        assertEquals(expected, resolver.htmlResourcePath(Controller.class));
    }

    @Test
    void htmlFileAbsolutePath() {
        @HtmlFile("/abs/path")
        class Controller {
        }
        Resources resources = mock(Resources.class);
        when(resources.exists("abs/path.html")).thenReturn(true);
        HtmlResourcePathResolver resolver = new HtmlResourcePathResolver(resources);

        assertEquals("abs/path.html", resolver.htmlResourcePath(Controller.class));
    }

    @Test
    void htmlFileRelativePath() {
        @HtmlFile("relpath")
        class Controller {
        }
        Resources resources = mock(Resources.class);
        when(resources.exists("one/xis/server/relpath.html")).thenReturn(true);
        HtmlResourcePathResolver resolver = new HtmlResourcePathResolver(resources);

        assertEquals("one/xis/server/relpath.html", resolver.htmlResourcePath(Controller.class));
    }

    @Test
    void defaultHtmlFileAbsolutePath() {
        @DefaultHtmlFile("/def/path")
        class Controller {
        }
        Resources resources = mock(Resources.class);
        when(resources.exists("def/path.html")).thenReturn(true);
        HtmlResourcePathResolver resolver = new HtmlResourcePathResolver(resources);

        assertEquals("def/path.html", resolver.htmlResourcePath(Controller.class));
    }

    @Test
    void defaultHtmlFileRelativePath() {
        @DefaultHtmlFile("defrel")
        class Controller {
        }
        Resources resources = mock(Resources.class);
        when(resources.exists("one/xis/server/defrel.html")).thenReturn(true);
        HtmlResourcePathResolver resolver = new HtmlResourcePathResolver(resources);

        assertEquals("one/xis/server/defrel.html", resolver.htmlResourcePath(Controller.class));
    }

    @Test
    void bothAnnotationsOnlyDefaultExists() {
        @HtmlFile("file1")
        @DefaultHtmlFile("file2")
        class Controller {
        }
        Resources resources = mock(Resources.class);
        when(resources.exists("one/xis/server/file1.html")).thenReturn(false);
        when(resources.exists("one/xis/server/file2.html")).thenReturn(true);
        HtmlResourcePathResolver resolver = new HtmlResourcePathResolver(resources);

        assertEquals("one/xis/server/file2.html", resolver.htmlResourcePath(Controller.class));
    }

    @Test
    void bothAnnotationsBothExist() {
        @HtmlFile("file1")
        @DefaultHtmlFile("file2")
        class Controller {
        }
        Resources resources = mock(Resources.class);
        when(resources.exists("one/xis/server/file1.html")).thenReturn(true);
        when(resources.exists("one/xis/server/file2.html")).thenReturn(true);
        HtmlResourcePathResolver resolver = new HtmlResourcePathResolver(resources);

        assertEquals("one/xis/server/file1.html", resolver.htmlResourcePath(Controller.class));
    }
}