package one.xis.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import one.xis.gson.GsonFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompactJsonPayloadTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Gson gson = new GsonFactory().gson();

    @Test
    void omitsEmptyServerResponseFields() throws Exception {
        var response = new ServerResponse();
        response.setStatus(200);
        response.getData().put("name", "Ada");

        String json = gson.toJson(response);

        assertThat(json).contains("\"data\":{\"name\":\"Ada\"}");
        assertThat(json).doesNotContain("formData");
        assertThat(json).doesNotContain("validatorMessages");
        assertThat(json).doesNotContain("closeModal");
        assertThat(json).doesNotContain("actionProcessing");
        assertThat(json).doesNotContain("userRoles");
    }

    @Test
    void keepsMeaningfulServerResponseFields() throws Exception {
        var response = new ServerResponse();
        response.setStatus(200);
        response.setCloseModal(true);
        response.getUpdateEventKeys().add("customers");
        response.getSessionStorageData().put("selectedCustomer", null);
        response.getUserRoles().add("ADMIN");

        String json = gson.toJson(response);

        assertThat(json).contains("\"closeModal\":true");
        assertThat(json).contains("\"updateEventKeys\":[\"customers\"]");
        assertThat(json).contains("\"sessionStorageData\":{\"selectedCustomer\":null}");
        assertThat(json).contains("\"userRoles\":[\"ADMIN\"]");
    }

    @Test
    void deserializesMissingClientRequestMapsAsEmptyMaps() throws Exception {
        ClientRequest request = objectMapper.readValue("""
                {
                  "clientId": "client-1",
                  "pageId": "/index.html",
                  "type": "page"
                }
                """, ClientRequest.class);

        assertThat(request.getClientId()).isEqualTo("client-1");
        assertThat(request.getFormData()).isEmpty();
        assertThat(request.getPathVariables()).isEmpty();
        assertThat(request.getActionParameters()).isEmpty();
        assertThat(request.getSessionStorageData()).isEmpty();
    }

    @Test
    void serializesOnlyMeaningfulClientRequestFields() throws Exception {
        var request = new ClientRequest();
        request.setClientId("client-1");
        request.setPageId("/index.html");
        request.setType(RequestType.page);
        request.getActionParameters().put("id", "42");
        request.getSessionStorageData().put("selectedCustomer", null);

        String json = objectMapper.writeValueAsString(request);

        assertThat(json).contains("\"clientId\":\"client-1\"");
        assertThat(json).contains("\"pageId\":\"/index.html\"");
        assertThat(json).contains("\"type\":\"page\"");
        assertThat(json).contains("\"actionParameters\":{\"id\":\"42\"}");
        assertThat(json).contains("\"sessionStorageData\":{\"selectedCustomer\":null}");
        assertThat(json).doesNotContain("formData");
        assertThat(json).doesNotContain("frontletParameters");
    }
}
