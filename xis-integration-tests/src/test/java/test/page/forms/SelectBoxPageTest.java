package test.page.forms;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Element;
import one.xis.test.dom.ElementImpl;
import one.xis.test.dom.Node;
import one.xis.test.dom.OptionElement;
import one.xis.test.dom.SelectElement;
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

    @BeforeEach
    void init() {
        selectBoxFormService = mock();
        context = IntegrationTestContext.builder()
                .withSingleton(SelectBoxPage.class)
                .withSingleton(SelectBoxForeachTagPage.class)
                .withSingleton(selectBoxFormService)
                .build();
    }

    @Test
    void optionsWithForeachTagInsideSelect() {
        SelectBoxFormModel selectBoxFormModel = new SelectBoxFormModel();
        selectBoxFormModel.setSelectedValue(2);

        when(selectBoxFormService.options()).thenReturn(List.of(new SelectBoxFormOption(2, "two")));
        when(selectBoxFormService.getSelectBoxFormModel()).thenReturn(selectBoxFormModel);

        var client = context.openPage(SelectBoxForeachTagPage.class);
        var form = client.getDocument().getElementByTagName("form");
        var selectBox = form.findDescendant(this::isSelect);

        assertThat(((Element) selectBox).findDescendants(this::isOption)).singleElement().satisfies(
                option -> {
                    assertThat(((Element) option).getAttribute("value")).isEqualTo("2");
                    assertThat(((ElementImpl) option).getInnerText()).isEqualTo("two");
                }
        );

        var submitButton = form.querySelector("button");
        var option = (OptionElement) client.getDocument().getElementByTagName("option");
        option.select();
        submitButton.click();

        var formModelCaptor = ArgumentCaptor.forClass(SelectBoxFormModel.class);
        verify(selectBoxFormService).saveSelectBoxFormModel(formModelCaptor.capture());

        assertThat(formModelCaptor.getValue().getSelectedValue()).isEqualTo(2);
    }

    private boolean isSelect(Node node) {
        return node instanceof ElementImpl element && "select".equals(element.getLocalName());
    }

    private boolean isOption(Node node) {
        return node instanceof ElementImpl element && "option".equals(element.getLocalName());
    }

    @Nested
    class EmptySelectBox {

        @BeforeEach
        void init() {
            reset(selectBoxFormService);

            SelectBoxFormModel selectBoxFormModel = new SelectBoxFormModel();
            selectBoxFormModel.setSelectedValue(2);

            when(selectBoxFormService.options()).thenReturn(List.of(new SelectBoxFormOption(2, "two")));
            when(selectBoxFormService.getSelectBoxFormModel()).thenReturn(selectBoxFormModel);
        }

        @Test
        void options() {
            var client = context.openPage(SelectBoxPage.class);
            var form = client.getDocument().getElementByTagName("form");
            var selectBox = form.findDescendant(this::isSelect);

            assertThat(((Element) selectBox).findDescendants(this::isOption)).singleElement().satisfies(
                    option -> {
                        assertThat(((Element) option).getAttribute("value")).isEqualTo("2");
                        assertThat(((ElementImpl) option).getInnerText()).isEqualTo("two");
                    }
            );

            var submitButton = form.querySelector("button");
            var option = (OptionElement) client.getDocument().getElementByTagName("option");
            option.select();
            submitButton.click();

            var formModelCaptor = ArgumentCaptor.forClass(SelectBoxFormModel.class);
            verify(selectBoxFormService).saveSelectBoxFormModel(formModelCaptor.capture());

            assertThat(formModelCaptor.getValue().getSelectedValue()).isEqualTo(2);
        }

        @Test
        void setValueReplacesPreviousSingleSelection() {
            when(selectBoxFormService.options()).thenReturn(List.of(
                    new SelectBoxFormOption(1, "one"),
                    new SelectBoxFormOption(2, "two")
            ));
            var formModel = new SelectBoxFormModel();
            formModel.setSelectedValue(1);
            when(selectBoxFormService.getSelectBoxFormModel()).thenReturn(formModel);

            var client = context.openPage(SelectBoxPage.class);
            var select = (SelectElement) client.getDocument().getElementById("selectBox");

            select.setValue("2");
            client.getDocument().getElementById("submitButton").click();

            var formModelCaptor = ArgumentCaptor.forClass(SelectBoxFormModel.class);
            verify(selectBoxFormService).saveSelectBoxFormModel(formModelCaptor.capture());

            assertThat(formModelCaptor.getValue().getSelectedValue()).isEqualTo(2);
        }

        private boolean isSelect(Node node) {
            return node instanceof ElementImpl element && "select".equals(element.getLocalName());
        }

        private boolean isOption(Node node) {
            return node instanceof ElementImpl element && "option".equals(element.getLocalName());
        }

    }
}
