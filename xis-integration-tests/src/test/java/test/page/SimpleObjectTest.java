package test.page;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Document;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SimpleObjectTest {


    private IntegrationTestContext testContext;

    @Nested
    @Disabled
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CreateNewObjectTest {

        private SimpleObjectService service;
        private Document document;

        @BeforeAll
        void init() {
            service = mock(SimpleObjectService.class);
            testContext = IntegrationTestContext.builder()
                    .withSingleton(service)
                    .withSingleton(SimpleObjectNewController.class)
                    .build();
        }

        @Test
        @Order(1)
        void openForm() {
            var result = testContext.openPage("/simpleObject/new.html");

            document = result.getDocument();
            var titleElement = document.getElementByTagName("title");

            assertThat(titleElement.innerText).isEqualTo("New Object");
        }

        @Test
        @Order(2)
        void test() {
            document.getElementById("field1").value = "v1";
            document.getElementById("field2").value = "v2";
            document.getElementByTagName("submit").onclick.accept(null);

            var captor = ArgumentCaptor.forClass(SimpleObject.class);
            verify(service).save(captor.capture());

            var titleElement = document.getElementByTagName("title");
            var p1 = document.getElementById("v1");
            var p2 = document.getElementById("v2");

            assertThat(titleElement.innerText).isEqualTo("Object Details");
            assertThat(p1.innerText).isEqualTo("p1");
            assertThat(p2.innerText).isEqualTo("p2");
        }
    }
}
