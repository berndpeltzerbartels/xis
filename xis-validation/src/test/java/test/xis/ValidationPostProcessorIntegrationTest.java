package test.xis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.context.IntegrationTestContext;
import one.xis.context.XISInit;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendService;
import one.xis.validation.EMail;
import one.xis.validation.LabelKey;
import one.xis.validation.Mandatory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationPostProcessorIntegrationTest {
    private FrontendService frontendService;
    private final ObjectMapper objectMapper = createObjectMapper();


    @BeforeEach
    void initFrontendService() {
        var context = IntegrationTestContext.builder()
                .withSingleton(PersonController.class)
                .build();
        frontendService = context.getSingleton(FrontendService.class);
    }

    @Test
    void validationOk() throws JsonProcessingException {
        // given
        var personData = new PersonData();
        personData.setName("Max Mustermann");
        personData.setEmail("bla@bla.de");
        personData.setDateOfBirth(LocalDate.of(2000, 1, 1));
        var request = createRequest(personData);

        // when
        var response = frontendService.processActionRequest(request);

        // then
        var dataTree = objectMapper.readTree(response.getData());
        personData = objectMapper.treeToValue(dataTree.at("/person"), PersonData.class);

        // Data was changed by the action method:
        assertThat(personData.getName()).isEqualTo("Maxl Mustermann");
        assertThat(personData.getEmail()).isEqualTo("blabla@blabla.de");
        assertThat(personData.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 5));
    }


    @Test
    void validateEmailOK() throws JsonProcessingException {
        // given
        var personData = new PersonData();
        personData.setName("Max Mustermann");
        personData.setEmail("bla@bla.de");
        personData.setDateOfBirth(LocalDate.of(2000, 1, 1));
        var request = createRequest(personData);

        // when
        var response = frontendService.processActionRequest(request);

        // then
        var dataTree = objectMapper.readTree(response.getData());
        personData = objectMapper.treeToValue(dataTree.at("/person"), PersonData.class);

        // Data was changed by the action method:
        assertThat(personData.getName()).isEqualTo("Maxl Mustermann");
        assertThat(personData.getEmail()).isEqualTo("blabla@blabla.de");
        assertThat(personData.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 5));

    }

    @Test
    void validateEmailFailed() throws JsonProcessingException {
        // given
        var personData = new PersonData();
        personData.setName("Max Mustermann");
        personData.setEmail("blabla.de");
        personData.setDateOfBirth(LocalDate.of(2000, 1, 1));
        var request = createRequest(personData);

        // when
        var response = frontendService.processActionRequest(request);

        // then
        assertThat(response.getHttpStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages()).containsEntry("/person/email", "Ungültig");
        assertThat(response.getValidatorMessages().getGlobalMessages()).containsExactly("Ungültige E-Mail-Adresse");
    }

    @Test
    void mandatoryFieldFailed() throws JsonProcessingException {
        // given
        var personData = new PersonData();
        personData.setName("Max Mustermann");
        var request = createRequest(personData);
        // when
        var response = frontendService.processActionRequest(request);

        // then
        assertThat(response.getHttpStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages()).containsExactlyInAnyOrderEntriesOf(Map.of("/person/email", "erforderlich", "/person/dateOfBirth", "erforderlich"));
        assertThat(response.getValidatorMessages().getGlobalMessages()).containsExactlyInAnyOrder("EMail ist erforderlich", "Geburtsdatum ist erforderlich");

    }

    private ClientRequest createRequest(PersonData data) throws JsonProcessingException {
        var personData = objectMapper.writeValueAsString(data);
        var request = new ClientRequest();
        request.setPageId("/person.html");
        request.setAction("save");
        request.setFormData(Map.of("person", personData));
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


    @Data
    static class PersonData {

        @Mandatory
        @LabelKey("name")
        private String name;

        @EMail
        @Mandatory
        @LabelKey("email")
        private String email;

        @Mandatory
        @LabelKey("dateOfBirth")
        private LocalDate dateOfBirth;
    }


    @Page("/person.html")
    static class PersonController {

        private PersonData personData;

        @XISInit
        void init() {
            personData = new PersonData();
            personData.setName("Max Mustermann");
            personData.setEmail("bla@bla.de");
            personData.setDateOfBirth(LocalDate.of(2000, 1, 1));
        }

        @ModelData("person")
        PersonData personData() {
            return personData;
        }

        @Action("save")
        void save(@FormData("person") PersonData person) {
            this.personData.setName("Maxl Mustermann");
            this.personData.setEmail("blabla@blabla.de");
            this.personData.setDateOfBirth(LocalDate.of(2000, 1, 5));
        }
    }


}
