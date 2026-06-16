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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
                .withSingleton(SelectBoxForeachTagTemplateFormPage.class)
                .withSingleton(SelectBoxForeachTagTemplateSelectPage.class)
                .withSingleton(SelectBoxIfTagPage.class)
                .withSingleton(SelectBoxPlaceholderForeachTagPage.class)
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

    @Test
    void pageBodyDoesNotExposeForeachTagInsideSelectToBrowserParser() {
        var body = pageBody(SelectBoxForeachTagPage.class);

        assertThat(body).doesNotContain("<xis:foreach");
        assertThat(body).contains("xis:repeat=\"option:options\"");
    }

    @Test
    void optionsWithForeachTagInsideSelectWhenFormIsInsideTemplate() {
        assertOptionsWithForeachTagInsideSelectWork(SelectBoxForeachTagTemplateFormPage.class);
    }

    @Test
    void pageBodyDoesNotExposeForeachTagInsideSelectWhenFormIsInsideTemplate() {
        var body = pageBody(SelectBoxForeachTagTemplateFormPage.class);

        assertThat(body).doesNotContain("<xis:foreach");
        assertThat(body).contains("xis:repeat=\"option:options\"");
    }

    @Test
    void optionsWithForeachTagInsideSelectWhenOnlySelectIsInsideTemplate() {
        assertOptionsWithForeachTagInsideSelectWork(SelectBoxForeachTagTemplateSelectPage.class);
    }

    @Test
    void pageBodyDoesNotExposeForeachTagInsideSelectWhenOnlySelectIsInsideTemplate() {
        var body = pageBody(SelectBoxForeachTagTemplateSelectPage.class);

        assertThat(body).doesNotContain("<xis:foreach");
        assertThat(body).contains("xis:repeat=\"option:options\"");
    }

    @Test
    void pageBodyDoesNotExposeIfTagInsideSelectToBrowserParser() {
        var body = pageBody(SelectBoxIfTagPage.class);

        assertThat(body).doesNotContain("<xis:if");
        assertThat(body).contains("xis:if=\"${showOption}\"");
    }

    @Test
    void optionWithIfTagInsideSelectStillRenders() {
        var client = context.openPage(SelectBoxIfTagPage.class);
        var option = client.getDocument().getElementByTagName("option");

        assertThat(option.getAttribute("value")).isEqualTo("visible");
        assertThat(option.getInnerText()).isEqualTo("Visible");
    }

    @Test
    void staticPlaceholderOptionAndForeachOptionsInsideSelectWorkTogether() {
        var client = context.openPage(SelectBoxPlaceholderForeachTagPage.class);
        var form = client.getDocument().getElementByTagName("form");
        var selectBox = form.findDescendant(this::isSelect);

        assertThat(((Element) selectBox).findDescendants(this::isOption))
                .hasSize(3)
                .satisfiesExactly(
                        option -> {
                            assertThat(((Element) option).getAttribute("value")).isEmpty();
                            assertThat(((ElementImpl) option).getInnerText()).isEqualTo("Please select");
                        },
                        option -> {
                            assertThat(((Element) option).getAttribute("value")).isEqualTo("light");
                            assertThat(((ElementImpl) option).getInnerText()).isEqualTo("Light");
                        },
                        option -> {
                            assertThat(((Element) option).getAttribute("value")).isEqualTo("dark");
                            assertThat(((ElementImpl) option).getInnerText()).isEqualTo("Dark");
                        }
                );
    }

    @Test
    void pageBodyKeepsStaticPlaceholderAndRewritesForeachOptionsInsideSelect() {
        var body = pageBody(SelectBoxPlaceholderForeachTagPage.class);

        assertThat(body).doesNotContain("<xis:foreach");
        assertThat(body).contains("<option value=\"\">Please select</option>");
        assertThat(body).contains("xis:repeat=\"option:options\"");
    }

    private boolean isSelect(Node node) {
        return node instanceof ElementImpl element && "select".equals(element.getLocalName());
    }

    private boolean isOption(Node node) {
        return node instanceof ElementImpl element && "option".equals(element.getLocalName());
    }

    private void assertOptionsWithForeachTagInsideSelectWork(Class<?> page) {
        SelectBoxFormModel selectBoxFormModel = new SelectBoxFormModel();
        selectBoxFormModel.setSelectedValue(2);

        when(selectBoxFormService.options()).thenReturn(List.of(new SelectBoxFormOption(2, "two")));
        when(selectBoxFormService.getSelectBoxFormModel()).thenReturn(selectBoxFormModel);

        var client = context.openPage(page);
        var form = client.getDocument().getElementByTagName("form");
        var selectBox = form.findDescendant(this::isSelect);

        assertThat(((Element) selectBox).findDescendants(this::isOption)).singleElement().satisfies(
                option -> {
                    assertThat(((Element) option).getAttribute("value")).isEqualTo("2");
                    assertThat(((ElementImpl) option).getInnerText()).isEqualTo("two");
                }
        );
    }

    private String pageBody(Class<?> page) {
        var pageId = one.xis.server.PageUtil.getUrl(page);
        var encodedPageId = URLEncoder.encode(pageId, StandardCharsets.UTF_8);
        var response = context.invokeBackend("GET", "/xis/page/body?pageId=" + encodedPageId);

        assertThat(response.status).isEqualTo(200);
        return response.responseText;
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
