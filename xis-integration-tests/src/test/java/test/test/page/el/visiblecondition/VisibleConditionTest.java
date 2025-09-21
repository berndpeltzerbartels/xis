package test.page.el.visiblecondition;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VisibleConditionTest {

    private IntegrationTestContext testContext;
    private VisibleConditionService service;

    @BeforeEach
    void init() {
        service = mock();
        testContext = IntegrationTestContext.builder()
                .withSingleton(VisibleConditionPage.class)
                .withSingleton(service)
                .build();

    }

    @Test
    void testVisibleConditionFalse() {
        when(service.getData()).thenReturn("123");
        var result = testContext.openPage("/visibleCondition.html");

        Element tagEmpty = result.getDocument().getElementById("visible-empty");
        assertThat(tagEmpty).isNull();
    }

    @Test
    void testVisibleConditionTrue() {
        when(service.getData()).thenReturn("");
        var result = testContext.openPage("/visibleCondition.html");

        Element tagEmpty = result.getDocument().getElementById("visible-empty");
        assertThat(tagEmpty).isNotNull();
        assertThat(tagEmpty.getInnerText()).isEqualTo("Inner text");
    }
}
