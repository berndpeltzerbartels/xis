package test.page.forms.validation.msg;

import one.xis.context.IntegrationTestContext;
import one.xis.gson.GsonFactory;
import one.xis.http.RequestContext;
import one.xis.server.FrontendServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Locale;

import static one.xis.gson.JsonMap.of;
import static org.assertj.core.api.Assertions.assertThat;

class MandatoryValidationRecordMessagesTest {

    private IntegrationTestContext context;
    private FrontendServiceImpl frontendService;

    @BeforeEach
    void setUp() {
        context = IntegrationTestContext.builder()
                .withSingleton(AllFormElementsRecordPage.class)
                .build();
        frontendService = context.getSingleton(FrontendServiceImpl.class);
        RequestContext.createInstance(null, null);
    }

    @Test
    void mandatoryValidationMessagesDefaultWithEmptyRecord() {
        // Empty record - all fields should trigger mandatory validation
        var model = new AllFormElementsRecordModel(null, null, null, null, null, null, null);
        var request = new one.xis.server.ClientRequest();
        request.setPageId("/allFormElementsRecord.html");
        request.setPageUrl("/allFormElementsRecord.html");
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
    void mandatoryValidationMessagesGermanWithEmptyRecord() {
        var model = new AllFormElementsRecordModel(null, null, null, null, null, null, null);
        var request = new one.xis.server.ClientRequest();
        request.setPageId("/allFormElementsRecord.html");
        request.setPageUrl("/allFormElementsRecord.html");
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

    @Test
    void recordWithValidData() {
        var model = new AllFormElementsRecordModel(
                "text",
                "textarea",
                true,
                "radio",
                "select",
                Collections.singletonList("item"),
                new String[]{"array"}
        );
        var request = new one.xis.server.ClientRequest();
        request.setPageId("/allFormElementsRecord.html");
        request.setPageUrl("/allFormElementsRecord.html");
        request.setAction("save");
        request.setFormData(of("formObject", new GsonFactory().gson().toJson(model)));
        request.setZoneId("Europe/London");
        request.setLocale(Locale.ENGLISH);

        var response = frontendService.processActionRequest(request);

        // Should succeed without validation errors
        assertThat(response.getStatus()).isNotEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages()).isEmpty();
    }

    @Test
    void recordWithMissingFieldsInJson() {
        // Simulate form submission with missing fields (like empty form fields)
        var request = new one.xis.server.ClientRequest();
        request.setPageId("/allFormElementsRecord.html");
        request.setPageUrl("/allFormElementsRecord.html");
        request.setAction("save");
        // Empty JSON object - all fields missing
        request.setFormData(of("formObject", "{}"));
        request.setZoneId("Europe/London");
        request.setLocale(Locale.ENGLISH);

        var response = frontendService.processActionRequest(request);

        // Should handle missing fields gracefully and trigger mandatory validation
        System.out.println("Response status: " + response.getStatus());
        System.out.println("Validation messages: " + response.getValidatorMessages().getMessages());
        System.out.println("Global messages: " + response.getValidatorMessages().getGlobalMessages());
        
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages().values())
                .contains("mandatory");
    }
}
