package test.page.forms.validation.annotation;

import one.xis.context.IntegrationTestContext;
import one.xis.gson.GsonFactory;
import one.xis.http.RequestContext;
import one.xis.Action;
import one.xis.FormData;
import one.xis.HtmlFile;
import one.xis.Page;
import one.xis.UserContext;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendServiceImpl;
import one.xis.validation.LabelKey;
import one.xis.validation.Validate;
import one.xis.validation.Validator;
import one.xis.validation.ValidatorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.util.Locale;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static one.xis.gson.JsonMap.of;
import static org.assertj.core.api.Assertions.assertThat;

class AnnotationValidationTest {

    private IntegrationTestContext context;
    private FrontendServiceImpl frontendService;

    static class TestModel {
        @NotNegative
        @LabelKey("order.total")
        int total = -5;

        @NotNegative
        @LabelKey("order.vat")
        int vat = -2;

        @NotNegative
        @LabelKey("order.springDiscount")
        int springDiscount = -1;
    }


    @BeforeEach
    void setUp() {
        context = IntegrationTestContext.builder()
                .withSingleton(TestPage.class)
                .withSingleton(TestRecordPage.class)
                .withSingleton(DiscountPage.class)
                .build();
        frontendService = context.getSingleton(FrontendServiceImpl.class);
        RequestContext.createInstance(null, null);
    }

    @Test
    void notNegativeValidationMessageWithLabel() {
        var model = new TestModel();
        var request = new ClientRequest();
        request.setPageId("/test.html");
        request.setPageUrl("/test.html");
        request.setAction("save");
        request.setFormData(of("formObject", new GsonFactory().gson().toJson(model)));
        request.setZoneId("Europe/Berlin");
        request.setLocale(Locale.GERMAN);

        var response = frontendService.processActionRequest(request);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages().values())
                .contains("negativ");
        assertThat(response.getValidatorMessages().getGlobalMessages()).containsExactlyInAnyOrder(
                "Der Wert für \"Gesamtpreis\" darf nicht negativ sein",
                "Der Wert für \"Umsatzsteuer\" darf nicht negativ sein",
                "Der Wert für \"aktueller Frühlingsrabatt\" darf nicht negativ sein"
        );
    }

    @Test
    void customValidationAnnotationAndLabelKeyWorkOnRecordComponents() {
        var model = new TestRecordModel(-5, -2, -1);
        var request = new ClientRequest();
        request.setPageId("/record-test.html");
        request.setPageUrl("/record-test.html");
        request.setAction("save");
        request.setFormData(of("formObject", new GsonFactory().gson().toJson(model)));
        request.setZoneId("Europe/Berlin");
        request.setLocale(Locale.GERMAN);

        var response = frontendService.processActionRequest(request);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages().values())
                .contains("negativ");
        assertThat(response.getValidatorMessages().getGlobalMessages()).containsExactlyInAnyOrder(
                "Der Wert für \"Gesamtpreis\" darf nicht negativ sein",
                "Der Wert für \"Umsatzsteuer\" darf nicht negativ sein",
                "Der Wert für \"aktueller Frühlingsrabatt\" darf nicht negativ sein"
        );
    }

    @Test
    void customValidationAnnotationWorksOnFormObject() {
        var model = new DiscountForm(80, 120);
        var request = new ClientRequest();
        request.setPageId("/discount-test.html");
        request.setPageUrl("/discount-test.html");
        request.setAction("save");
        request.setFormData(of("formObject", new GsonFactory().gson().toJson(model)));
        request.setZoneId("Europe/Berlin");
        request.setLocale(Locale.GERMAN);

        var response = frontendService.processActionRequest(request);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages())
                .containsEntry("/formObject", "discount exceeds subtotal");
        assertThat(response.getValidatorMessages().getGlobalMessages())
                .containsExactly("Discount must not exceed subtotal");
    }

    record TestRecordModel(
            @NotNegative
            @LabelKey("order.total")
            int total,

            @NotNegative
            @LabelKey("order.vat")
            int vat,

            @NotNegative
            @LabelKey("order.springDiscount")
            int springDiscount
    ) {
    }

    @Retention(RUNTIME)
    @Target(TYPE)
    @Validate(
            validatorClass = DiscountValidator.class,
            messageKey = "validation.discount",
            globalMessageKey = "validation.discount.global"
    )
    @interface ValidDiscount {
    }

    @ValidDiscount
    record DiscountForm(int subtotal, int discount) {
    }

    static class DiscountValidator implements Validator<DiscountForm> {
        @Override
        public void validate(DiscountForm value, AnnotatedElement target, UserContext userContext)
                throws ValidatorException {
            if (value.discount() > value.subtotal()) {
                throw new ValidatorException();
            }
        }
    }

    @Page("/record-test.html")
    @HtmlFile("/TestPage.html")
    static class TestRecordPage {

        @Action
        void save(@FormData("formObject") TestRecordModel model) {
            // Validation happens before this action can use the record DTO.
        }
    }

    @Page("/discount-test.html")
    @HtmlFile("/TestPage.html")
    static class DiscountPage {

        @Action
        void save(@FormData("formObject") DiscountForm form) {
            // Object validation happens before this action can use the DTO.
        }
    }
}
