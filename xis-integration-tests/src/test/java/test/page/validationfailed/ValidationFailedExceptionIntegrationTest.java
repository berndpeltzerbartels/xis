package test.page.validationfailed;

import one.xis.context.IntegrationTestContext;
import one.xis.http.RequestContext;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationFailedExceptionIntegrationTest {
    private IntegrationTestContext context;
    private FrontendService frontendService;

    @BeforeEach
    void setUp() {
        context = IntegrationTestContext.builder()
                .withPackage("test.page.validationfailed")
                .build();
        frontendService = context.getSingleton(FrontendService.class);
        RequestContext.createInstance(null, null);
    }

    @Test
    void actionValidationFailedExceptionBecomesValidatorMessages() {
        var response = frontendService.processActionRequest(actionRequest());

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getGlobalMessages())
                .containsExactly("Invalid username or password");
        assertThat(response.getValidatorMessages().getMessages())
                .containsEntry("/form/name", "invalid");
        assertThat(response.getData()).containsEntry("value", "loaded");
    }

    private ClientRequest actionRequest() {
        var request = new ClientRequest();
        request.setPageId("/validation-failed.html");
        request.setPageUrl("/validation-failed.html");
        request.setAction("reject");
        request.setLocale(Locale.ENGLISH);
        request.setZoneId("Europe/London");
        return request;
    }
}
