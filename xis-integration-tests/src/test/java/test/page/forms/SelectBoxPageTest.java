package test.page.forms;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.OptionElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SelectBoxPageTest {

    private IntegrationTestContext context;
    private SelectBoxFormService selectBoxFormService;
    private SelectBoxFormModel selectBoxFormModel;

    @BeforeEach
    void init() {
        selectBoxFormService = mock();
        context = IntegrationTestContext.builder()
                .withSingleton(SelectBoxPage.class)
                .withSingleton(selectBoxFormService)
                .build();
    }

    @Nested
    class EmptySelectBox {

        @BeforeEach
        void init() {
            reset(selectBoxFormService);

            selectBoxFormModel = new SelectBoxFormModel();
            selectBoxFormModel.setSelectedValue(2);

            when(selectBoxFormService.options()).thenReturn(List.of(new SelectBoxFormOption(2, "two")));
            when(selectBoxFormService.getSelectBoxFormModel()).thenReturn(selectBoxFormModel);
        }

        @Test
        void options() {
            var result = context.openPage(SelectBoxPage.class);
            var form = result.getDocument().getElementByTagName("form");
            var selectBox = form.findDescendant(element -> "select".equals(element.getLocalName()));

            assertThat(selectBox.findDescendants(e -> e.getLocalName().equals("option"))).singleElement().satisfies(
                    option -> {
                        assertThat(option.getAttribute("value")).isEqualTo("2");
                        assertThat(option.getInnerText()).isEqualTo("two");
                    }
            );

            var submitButton = form.querySelector("button");
            var option = (OptionElement) result.getDocument().getElementByTagName("option");
            option.select();
            submitButton.click();

            var formModelCaptor = ArgumentCaptor.forClass(SelectBoxFormModel.class);
            verify(selectBoxFormService).saveSelectBoxFormModel(formModelCaptor.capture());

            assertThat(formModelCaptor.getValue().getSelectedValue()).isEqualTo(2);
        }

    }
}