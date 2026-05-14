package test.page.core;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.DragAndDrop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DragDropPageTest {

    private DragDropService service;
    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        service = mock(DragDropService.class);
        testContext = IntegrationTestContext.builder()
                .withSingleton(DragDropPage.class)
                .withSingleton(service)
                .build();
    }

    @Test
    void dropCallsActionWithDragValueAndDropValue() {
        var client = testContext.openPage("/dragDrop.html");

        new DragAndDrop(
                client.getDocument().getElementById("source"),
                client.getDocument().getElementById("target")
        ).doDragAndDrop();

        verify(service).move("a2", "a4");
    }

    @Test
    void explicitParameterIndexesAreOneBasedAndCountOnlyActionArguments() {
        var client = testContext.openPage("/dragDrop.html");

        new DragAndDrop(
                client.getDocument().getElementById("source"),
                client.getDocument().getElementById("target-explicit-index")
        ).doDragAndDrop();

        verify(service).move("a2", "a4");
    }

}
