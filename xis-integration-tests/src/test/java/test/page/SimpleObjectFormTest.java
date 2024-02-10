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
        private SimpleObject simpleObject;

        @BeforeAll
        void init() {
            service = mock(SimpleObjectService.class);
            testContext = IntegrationTestContext.builder()
                    .withSingleton(service)
                    .withSingleton(SimpleObjectNewController.class)
                    .withSingleton(SimpleObjectDetails.class)
                    .build();

            doAnswer(inv -> {
                simpleObject = inv.getArgument(0);
                simpleObject.setId(1000);
                return null;
            }).when(service).save(any());

            doAnswer(inv -> {
                Integer id = inv.getArgument(0);
                return id != null && id.equals(1000) ? simpleObject : null;
            }).when(service).getById(eq(1000));
        }

        @Test
        void test() {
            // Display form
            var result = testContext.openPage("/simpleObject/new.html");

            var document = result.getDocument();
            var titleElement = document.getElementByTagName("title");
            var inputField1 = document.getInputElementById("field1");
            var inputField2 = document.getInputElementById("field2");

            // Check controller values
            assertThat(titleElement.innerText).isEqualTo("New Object");
            assertThat(inputField1.value).isEqualTo("p1");
            assertThat(inputField2.value).isEqualTo("p2");

            // Edit field values
            inputField1.value = "v1";
            inputField2.value = "v2";
            document.getElementById("save").click();
            assertThat(simpleObject.getProperty1()).isEqualTo("v1");
            assertThat(simpleObject.getProperty2()).isEqualTo("v2");

            verify(service).save(any());

            // After saving object, the details page should be displayed
            assertThat(titleElement.innerText).isEqualTo("Object Details");

            // The details page should display the saved values
            var p1 = document.getElementById("v1");
            var p2 = document.getElementById("v2");
            assertThat(p1.innerText).isEqualTo("v1");
            assertThat(p2.innerText).isEqualTo("v2");
        }

    }
}
