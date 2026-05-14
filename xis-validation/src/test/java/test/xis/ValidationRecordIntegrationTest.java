package test.xis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import one.xis.*;
import one.xis.context.IntegrationTestContext;
import one.xis.context.Init;
import one.xis.gson.JsonMap;
import one.xis.http.RequestContext;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendServiceImpl;
import one.xis.validation.EMail;
import one.xis.validation.LabelKey;
import one.xis.validation.Mandatory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationRecordIntegrationTest {
    private final ObjectMapper objectMapper = createObjectMapper();
    private FrontendServiceImpl frontendService;
    private IntegrationTestContext context;

    @BeforeEach
    void initFrontendService() {
        context = IntegrationTestContext.builder()
                .withSingleton(UserRecordController.class)
                .build();
        frontendService = context.getSingleton(FrontendServiceImpl.class);
        RequestContext.createInstance(null, null);
    }

    @Test
    void validationOk() throws JsonProcessingException {
        // given
        var userData = new UserRecord("Max Mustermann", "max@example.com", LocalDate.of(1990, 5, 15));
        var request = createUserRequest(userData);

        // when
        var response = frontendService.processActionRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        var dataTree = objectMapper.valueToTree(response.getFormData());
        var savedUser = objectMapper.treeToValue(dataTree.at("/user"), UserRecord.class);

        assertThat(savedUser.name()).isEqualTo("Max Mustermann");
        assertThat(savedUser.email()).isEqualTo("max@example.com");
        assertThat(savedUser.dateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
    }

    @Test
    void validateEmailFailed() throws JsonProcessingException {
        // given
        var userData = new UserRecord("Max Mustermann", "invalid-email", LocalDate.of(1990, 5, 15));
        var request = createUserRequest(userData);

        // when
        var response = frontendService.processActionRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages()).containsEntry("/user/email", "Ungültig");
        assertThat(response.getValidatorMessages().getGlobalMessages()).containsExactly("Ungültige E-Mail-Adresse");
    }

    @Test
    void mandatoryNameFieldFailed() throws JsonProcessingException {
        // given
        var userData = new UserRecord(null, "max@example.com", LocalDate.of(1990, 5, 15));
        var request = createUserRequest(userData);

        // when
        var response = frontendService.processActionRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages()).containsEntry("/user/name", "erforderlich");
        assertThat(response.getValidatorMessages().getGlobalMessages()).contains("[name] ist erforderlich");
    }

    @Test
    void mandatoryEmailFieldFailed() throws JsonProcessingException {
        // given
        var userData = new UserRecord("Max Mustermann", null, LocalDate.of(1990, 5, 15));
        var request = createUserRequest(userData);

        // when
        var response = frontendService.processActionRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages()).containsEntry("/user/email", "erforderlich");
        assertThat(response.getValidatorMessages().getGlobalMessages()).contains("EMail ist erforderlich");
    }

    @Test
    void multipleMandatoryFieldsFailed() throws JsonProcessingException {
        // given
        var userData = new UserRecord(null, null, null);
        var request = createUserRequest(userData);

        // when
        var response = frontendService.processActionRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages()).containsExactlyInAnyOrderEntriesOf(
                Map.of(
                        "/user/name", "erforderlich",
                        "/user/email", "erforderlich",
                        "/user/dateOfBirth", "erforderlich"
                )
        );
    }

    @Test
    void emailInvalidAndDateOfBirthMissing() throws JsonProcessingException {
        // given
        var userData = new UserRecord("Max Mustermann", "not-an-email", null);
        var request = createUserRequest(userData);

        // when
        var response = frontendService.processActionRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages()).containsEntry("/user/email", "Ungültig");
        assertThat(response.getValidatorMessages().getMessages()).containsEntry("/user/dateOfBirth", "erforderlich");
    }

    private ClientRequest createUserRequest(UserRecord data) throws JsonProcessingException {
        var userData = objectMapper.writeValueAsString(data);
        var request = new ClientRequest();
        request.setPageId("/user.html");
        request.setPageUrl("/user.html");
        request.setAction("save");
        request.setFormData(JsonMap.of("user", userData));
        request.setLocale(Locale.GERMANY);
        request.setZoneId("Europe/Berlin");
        return request;
    }

    private ObjectMapper createObjectMapper() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    record UserRecord(
            @Mandatory
            @LabelKey("name")
            String name,

            @EMail
            @Mandatory
            @LabelKey("email")
            String email,

            @Mandatory
            @LabelKey("dateOfBirth")
            LocalDate dateOfBirth
    ) {
    }

    @HtmlFile("/UserRecordController.html")
    @Page("/user.html")
    static class UserRecordController {

        @Getter
        private UserRecord userData;

        @Init
        void init() {
            userData = new UserRecord("Max Mustermann", "max@example.com", LocalDate.of(1990, 5, 15));
        }

        @ModelData("user")
        UserRecord userData() {
            return userData;
        }

        @Action("save")
        void save(@FormData("user") UserRecord user) {
            this.userData = user;
        }
    }
}
