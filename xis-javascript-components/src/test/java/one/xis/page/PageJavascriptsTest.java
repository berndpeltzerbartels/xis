package one.xis.page;

import one.xis.context.TestContext;
import one.xis.resource.ReloadableResourceFile;
import one.xis.resource.ResourceFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PageJavascriptsTest {

    private PageJavascripts pageJavascripts;
    private PageJavascriptCompiler pageJavascriptCompiler;
    private PageFactory pageFactory;
    private ReloadableResourceFile resourceFile;

    @one.xis.Page(path = "/xyz/test.html")
    class PageController {

    }

    @BeforeEach
    void init() {
        PageController pageController = new PageController();

        ResourceFiles resourceFiles = mock(ResourceFiles.class);
        pageFactory = new PageFactory(resourceFiles);
        pageJavascriptCompiler = mock(PageJavascriptCompiler.class);
        resourceFile = mock(ReloadableResourceFile.class);

        when(resourceFiles.getByPath(any())).thenReturn(resourceFile);
        when(resourceFile.getContent()).thenReturn("");

        TestContext testContext = TestContext.builder()//
                .withSingleton(PageJavascripts.class)//
                .withMockedSingleton(pageFactory)//
                .withMockedSingletons(pageJavascriptCompiler)//
                .build();

        pageJavascripts = testContext.getSingleton(PageJavascripts.class);
        pageJavascripts.add("xyz:test", pageController);
    }

    @Nested
    class GetActualPage {

        @BeforeEach
        void init() {
            when(resourceFile.isObsolete()).thenReturn(false);
        }

        @Test
        void getPage() {
            PageJavascript pageJavascript = pageJavascripts.get("xyz:test");

            assertThat(pageJavascript).isNotNull();
            verify(pageJavascriptCompiler, times(1)).compile(anyString(), eq(resourceFile), anyString());
        }
    }


    @Nested
    class RecompilePage {

        @BeforeEach
        void init() {
            when(resourceFile.isObsolete()).thenReturn(true);
        }

        @Test
        void getPage() {
            PageJavascript pageJavascript = pageJavascripts.get("xyz:test");

            assertThat(pageJavascript).isNotNull();
            verify(pageJavascriptCompiler, times(2)).compile(anyString(), eq(resourceFile), anyString());
        }
    }
}
