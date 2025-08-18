package test.page.forms.validation.annotation;

import one.xis.context.IntegrationTestContext;
import one.xis.gson.GsonFactory;
import one.xis.http.RequestContext;
import one.xis.server.FrontendServiceImpl;
import one.xis.validation.LabelKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static one.xis.gson.JsonMap.of;
import static org.assertj.core.api.Assertions.assertThat;

class AnnotationValidationTest {

    private IntegrationTestContext context;
    private FrontendServiceImpl frontendService;

    static class TestModel {
        @NotNegative
        @LabelKey("amountLabel")
        int amount = -5; // ungültiger Wert
    }


    @BeforeEach
    void setUp() {
        context = IntegrationTestContext.builder()
                .withSingleton(TestPage.class)
                .build();
        frontendService = context.getSingleton(FrontendServiceImpl.class);
        RequestContext.createInstance(null, null);
    }

    @Test
    void notNegativeValidationMessageWithLabel() {
        var model = new TestModel();
        var request = new one.xis.server.ClientRequest();
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
        assertThat(response.getValidatorMessages().getGlobalMessages()).contains("Der Wert für \"Betrag\" darf nicht negativ sein");
    }
}