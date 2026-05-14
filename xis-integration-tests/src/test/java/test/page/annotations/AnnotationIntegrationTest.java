package test.page.annotations;

import one.xis.auth.UserInfoImpl;
import one.xis.context.IntegrationTestContext;
import one.xis.gson.JsonMap;
import one.xis.http.RequestContext;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationIntegrationTest {

    @Test
    void welcomePageDefaultHtmlFileAndUserIdAreUsedByBrowserLifecycle() {
        var userInfo = new UserInfoImpl();
        userInfo.setUserId("annotation-user");
        var context = IntegrationTestContext.builder()
                .withLoggedInUser(userInfo, "secret")
                .withSingleton(AnnotationWelcomePage.class)
                .build();

        var client = context.openPage("/");

        assertThat(client.getDocument().getElementByTagName("title").getInnerText())
                .isEqualTo("Annotation Welcome");
        assertThat(client.getDocument().getElementById("message").getInnerText())
                .isEqualTo("Welcome annotation resolved");
        assertThat(client.getDocument().getElementById("user-id").getInnerText())
                .isEqualTo("annotation-user");
    }

    @Test
    void formatterValidationAnnotationsAndNullAllowedParticipateInActionLifecycle() {
        var context = IntegrationTestContext.builder()
                .withSingleton(AnnotationValidationPage.class)
                .build();
        var frontendService = context.getSingleton(FrontendServiceImpl.class);
        RequestContext.createInstance(null, null);

        var validResponse = frontendService.processActionRequest(saveRequest("""
                {"dueDate":"31.12.2026","email":"test@example.com","name":"Ada","numbers":[1,2,3]}
                """));

        assertThat(validResponse.getStatus()).isEqualTo(200);
        assertThat(context.getSingleton(AnnotationValidationPage.class).getSavedForm().dueDate())
                .isEqualTo(LocalDate.of(2026, 12, 31));

        var invalidResponse = frontendService.processActionRequest(saveRequest("""
                {"dueDate":"31.12.2026","email":"not-an-email","name":"Al","numbers":[1,"x"]}
                """));

        assertThat(invalidResponse.getStatus()).isEqualTo(422);
        assertThat(invalidResponse.getValidatorMessages().getMessages().keySet())
                .anySatisfy(key -> assertThat(key).contains("email"))
                .anySatisfy(key -> assertThat(key).contains("name"))
                .anySatisfy(key -> assertThat(key).contains("numbers"));

        var optionalResponse = frontendService.processActionRequest(optionalRequest());

        assertThat(optionalResponse.getStatus()).isEqualTo(200);
        assertThat(optionalResponse.getData()).containsEntry("optionalResult", "missing");
    }

    private ClientRequest saveRequest(String formJson) {
        var request = baseRequest();
        request.setAction("save");
        request.setFormData(JsonMap.of("form", formJson));
        return request;
    }

    private ClientRequest optionalRequest() {
        var request = baseRequest();
        request.setAction("optional");
        return request;
    }

    private ClientRequest baseRequest() {
        var request = new ClientRequest();
        request.setPageId("/annotation-validation.html");
        request.setPageUrl("/annotation-validation.html");
        request.setLocale(Locale.GERMANY);
        request.setZoneId("Europe/Berlin");
        return request;
    }
}
