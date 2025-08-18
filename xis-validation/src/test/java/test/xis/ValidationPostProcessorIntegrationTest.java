package test.xis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.Getter;
import one.xis.*;
import one.xis.context.IntegrationTestContext;
import one.xis.context.XISInit;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationPostProcessorIntegrationTest {
    private FrontendServiceImpl frontendService;
    private IntegrationTestContext context;
    private final ObjectMapper objectMapper = createObjectMapper();


    @BeforeEach
    void initFrontendService() {
        context = IntegrationTestContext.builder()
                .withSingleton(PersonController.class)
                .withSingleton(PersonDataListController.class)
                .build();
        frontendService = context.getSingleton(FrontendServiceImpl.class);
        RequestContext.createInstance(null, null);
    }

    @Test
    void validationOk() throws JsonProcessingException {
        // given
        var personData = new PersonData();
        personData.setName("Max Mustermann");
        personData.setEmail("bla@bla.de");
        personData.setDateOfBirth(LocalDate.of(2000, 1, 1));
        var request = createPersonRequest(personData);

        // when
        var response = frontendService.processActionRequest(request);

        // then
        var dataTree = objectMapper.valueToTree(response.getFormData());
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
        var request = createPersonRequest(personData);

        // when
        var response = frontendService.processActionRequest(request);

        // then
        var dataTree = objectMapper.valueToTree(response.getFormData());
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
        var request = createPersonRequest(personData);

        // when
        var response = frontendService.processActionRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages()).containsEntry("/person/email", "Ungültig");
        assertThat(response.getValidatorMessages().getGlobalMessages()).containsExactly("Ungültige E-Mail-Adresse");
    }

    @Test
    void mandatoryFieldFailed() throws JsonProcessingException {
        // given
        var personData = new PersonData();
        personData.setName("Max Mustermann");
        var request = createPersonRequest(personData);
        // when
        var response = frontendService.processActionRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages()).containsExactlyInAnyOrderEntriesOf(Map.of("/person/email", "erforderlich", "/person/dateOfBirth", "erforderlich"));
        assertThat(response.getValidatorMessages().getGlobalMessages()).containsExactlyInAnyOrder("EMail ist erforderlich", "Geburtsdatum ist erforderlich");
    }

    @Test
    void saveListOk() throws JsonProcessingException {
        // given
        var personDataList = new ArrayList<PersonData>();
        var personData = new PersonData();
        personData.setName("Max Mustermann");
        personData.setEmail("bla@bla.de");
        personData.setDateOfBirth(LocalDate.of(2000, 1, 1));
        personDataList.add(personData);
        var request = createPersonListRequest(personDataList);

        // when
        var response = frontendService.processActionRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(context.getSingleton(PersonDataListController.class).getPersons()).containsExactly(personData);
    }

    @Test
    void saveListFailed() {
        // given
        var personDataList = """
                [
                    {
                        "name": "Max Mustermann",
                        "dateOfBirth": "2000-01-01"
                    },
                    {
                        "name": "Hansl Fransl",
                        "email": "bla.de",
                        "dateOfBirth": "erster Januar 2000"
                    }
                ]
                """;

        var request = new ClientRequest();
        request.setPageId("/person-list.html");
        request.setPageUrl("/person-list.html");
        request.setAction("save");
        request.setLocale(Locale.GERMANY);
        request.setZoneId("Europe/Berlin");
        request.setFormData(JsonMap.of("persons", personDataList));

        // when
        var response = frontendService.processActionRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getValidatorMessages().getMessages()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "/persons[0]/email", "erforderlich",
                "/persons[1]/email", "Ungültig",
                "/persons[1]/dateOfBirth", "Ungültige Eingabe"
        ));

        assertThat(response.getValidatorMessages().getGlobalMessages())
                .containsExactlyInAnyOrder("EMail ist erforderlich",
                        "Ungültige E-Mail-Adresse",
                        "Bitte überprüfen Sie Ihre Eingabe für das Feld \"Geburtsdatum\"");
    }

    private ClientRequest createPersonRequest(PersonData data) throws JsonProcessingException {
        var personData = objectMapper.writeValueAsString(data);
        var request = new ClientRequest();
        request.setPageId("/person.html");
        request.setPageUrl("/person.html");
        request.setAction("save");
        request.setFormData(JsonMap.of("person", personData));
        request.setLocale(Locale.GERMANY);
        request.setZoneId("Europe/Berlin");
        return request;
    }

    private ClientRequest createPersonListRequest(List<PersonData> data) throws JsonProcessingException {
        var personDataList = objectMapper.writeValueAsString(data);
        var request = new ClientRequest();
        request.setPageId("/person-list.html");
        request.setPageUrl("/person-list.html");
        request.setAction("save");
        request.setLocale(Locale.GERMANY);
        request.setZoneId("Europe/Berlin");
        request.setFormData(JsonMap.of("persons", personDataList));
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

        @Mandatory // No @LabelKey annotation to check if the default message-key is used
        private LocalDate dateOfBirth;
    }


    @HtmlFile("/PersonController.html")
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
            person.setName("Maxl Mustermann");
            person.setEmail("blabla@blabla.de");
            person.setDateOfBirth(LocalDate.of(2000, 1, 5));
            personData = person;
        }
    }

    @HtmlFile("/PersonDataListController.html")
    @Page("/person-list.html")
    static class PersonDataListController {

        @Getter
        private List<PersonData> persons = new ArrayList<>();

        @ModelData("persons")
        List<PersonData> persons() {
            return persons;
        }

        @Action("add")
        void add(@FormData("person") PersonData person) {
            persons.add(person);
        }

        @Action("save")
        void save(@FormData("persons") List<PersonData> persons) {
            this.persons = persons;
        }
    }


}
