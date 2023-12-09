package test.page;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Document;
import one.xis.test.js.Event;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SimpleObjectFormTest {


    private IntegrationTestContext testContext;

    @Nested
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
                    .withSingleton(SimpleObjectDetails.class)
                    .build();
            when(service.getById(eq(1))).thenReturn(new SimpleObject(1, "SimpleObject", "p1", "p2"));
        }

        @Test
        void test() {
            var result = testContext.openPage("/simpleObject/new.html");

            document = result.getDocument();
            var titleElement = document.getElementByTagName("title");

            assertThat(titleElement.innerText).isEqualTo("New Object");

            document.getElementById("field1").value = "v1";
            document.getElementById("field2").value = "v2";
            document.getElementById("save").onclick.accept(new Event());

            var captor = ArgumentCaptor.forClass(SimpleObject.class);
            verify(service).save(captor.capture());

            assertThat(titleElement.innerText).isEqualTo("Object Details");

            var p1 = document.getElementById("v1");
            var p2 = document.getElementById("v2");


            assertThat(p1.innerText).isEqualTo("p1");
            assertThat(p2.innerText).isEqualTo("p2");
        }

    }
}
