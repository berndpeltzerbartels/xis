package one.xis.jsc;

import one.xis.context.TestContext;
import one.xis.resource.ReloadableResourceFile;
import one.xis.resource.ResourceFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PagesTest {

    private Pages pages;
    private PageCompiler pageCompiler;
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
        pageCompiler = mock(PageCompiler.class);
        resourceFile = mock(ReloadableResourceFile.class);

        when(resourceFiles.getByPath(any())).thenReturn(resourceFile);
        when(resourceFile.getContent()).thenReturn("");

        TestContext testContext = TestContext.builder()//
                .withSingleton(Pages.class)//
                .withMockedSingleton(pageFactory)//
                .withMockedSingletons(pageCompiler)//
                .build();

        pages = testContext.getSingleton(Pages.class);
        pages.add(pageController.getClass().getName(), pageController);
    }

    @Nested
    class GetActualPage {

        @BeforeEach
        void init() {
            when(resourceFile.isObsolete()).thenReturn(false);
        }

        @Test
        void getPage() {
            Page page = pages.get("xyz:test");

            assertThat(page).isNotNull();
            verify(pageCompiler, times(1)).compile(anyString(), eq(resourceFile));
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
            Page page = pages.get("xyz:test");

            assertThat(page).isNotNull();
            verify(pageCompiler, times(2)).compile(anyString(), eq(resourceFile));
        }
    }
}
