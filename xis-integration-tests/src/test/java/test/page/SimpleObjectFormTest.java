package test.page;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SimpleObjectFormTest {


    private IntegrationTestContext testContext;

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CreateNewObjectTest {

        private SimpleObjectService service;

        @BeforeAll
        void init() {
            service = mock(SimpleObjectService.class);
            testContext = IntegrationTestContext.builder()
                    .withSingleton(service)
                    .withSingleton(SimpleObjectNewController.class)
                    .withSingleton(SimpleObjectDetails.class)
                    .build();

            doAnswer(inv -> {
                var simpleObject = (SimpleObject) inv.getArgument(0);
                simpleObject.setId(1000);
                return simpleObject;
            }).when(service).save(any());

            when(service.getById(eq(1000))).thenReturn(new SimpleObject(1000, "SimpleObject", "p1", "p2"));
        }

        @Test
        void test() {
            var result = testContext.openPage("/simpleObject/new.html");

            var document = result.getDocument();
            var titleElement = document.getElementByTagName("title");

            assertThat(titleElement.innerText).isEqualTo("New Object");

            document.getElementById("field1").value = "v1";
            document.getElementById("field2").value = "v2";
            document.getElementById("save").click();

            verify(service).save(any());

            assertThat(titleElement.innerText).isEqualTo("Object Details");

            var p1 = document.getElementById("v1");
            var p2 = document.getElementById("v2");


            assertThat(p1.innerText).isEqualTo("p1");
            assertThat(p2.innerText).isEqualTo("p2");
        }

    }
}
