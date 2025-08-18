package test.page.forms.validation.msg;

import one.xis.context.IntegrationTestContext;
import one.xis.gson.GsonFactory;
import one.xis.http.RequestContext;
import one.xis.server.FrontendServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static one.xis.gson.JsonMap.of;
import static org.assertj.core.api.Assertions.assertThat;

class MandatoryValidationMessagesTest {

    private IntegrationTestContext context;
    private FrontendServiceImpl frontendService;

    @BeforeEach
    void setUp() {
        context = IntegrationTestContext.builder()
                .withSingleton(AllFormElementsPage.class)
                .build();
        frontendService = context.getSingleton(FrontendServiceImpl.class);
        RequestContext.createInstance(null, null);
    }

    @Test
    void mandatoryValidationMessagesDefault() {
        var model = new AllFormElementsModel();
        var request = new one.xis.server.ClientRequest();
        request.setPageId("/allFormElements.html");
        request.setPageUrl("/allFormElements.html");
        request.setAction("save");
        request.setFormData(of("formObject", new GsonFactory().gson().toJson(model)));
        request.setZoneId("Europe/London");
        request.setLocale(Locale.ENGLISH);

        var response = frontendService.processActionRequest(request);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages().values())
                .contains("mandatory");
        assertThat(response.getValidatorMessages().getGlobalMessages())
                .contains("Custom global mandatory message");
    }

    @Test
    void mandatoryValidationMessagesGerman() {
        var model = new AllFormElementsModel();
        var request = new one.xis.server.ClientRequest();
        request.setPageId("/allFormElements.html");
        request.setPageUrl("/allFormElements.html");
        request.setAction("save");
        request.setFormData(of("formObject", new GsonFactory().gson().toJson(model)));
        request.setZoneId("Europe/Berlin");
        request.setLocale(Locale.GERMAN);

        var response = frontendService.processActionRequest(request);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages().values())
                .contains("Benutzerdefinierte Pflichtfeldmeldung");
        assertThat(response.getValidatorMessages().getGlobalMessages())
                .contains("Benutzerdefinierte globale Pflichtfeldmeldung");
    }
}