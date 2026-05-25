package test.frontlet.samefrontletresponse;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SameFrontletResponseTest {

    private IntegrationTestContext context;

    @BeforeEach
    void init() {
        context = IntegrationTestContext.builder()
                .withSingleton(SameFrontletResponsePage.class)
                .withSingleton(SameFrontlet.class)
                .build();
    }

    @Test
    void sameFrontletResponseKeepsReturnedParameters() {
        var client = context.openPage("/same-frontlet-response.html");
        var document = client.getDocument();
        var queryFrontlet = frontlet(document, "query");
        var tagFrontlet = frontlet(document, "tag");

        assertFrontletValues(queryFrontlet, "1", "1");
        assertFrontletValues(tagFrontlet, "1", "1");

        var response = context.invokeBackend("POST", "/xis/frontlet/action", Map.of(), """
                {
                  "clientId": "test",
                  "pageId": "/same-frontlet-response.html",
                  "pageUrl": "/same-frontlet-response.html",
                  "frontletId": "SameFrontlet",
                  "action": "selectStep",
                  "zoneId": "Europe/Berlin",
                  "frontletParameters": {
                    "pipelineId": "\\"1\\"",
                    "stepId": "\\"1\\""
                  },
                  "type": "frontlet"
                }
                """);
        assertThat(response.getResponseText()).contains("nextFrontletId");
        assertThat(response.getResponseText()).contains("\"stepId\":2");

        queryFrontlet.querySelector("#select-step").click();

        document = client.getDocument();
        queryFrontlet = frontlet(document, "query");
        tagFrontlet = frontlet(document, "tag");
        assertFrontletValues(queryFrontlet, "1", "2");
        assertFrontletValues(tagFrontlet, "1", "1");
        assertThat(SameFrontlet.lastStep).isEqualTo("2");

        tagFrontlet.querySelector("#select-tagged-step").click();

        document = client.getDocument();
        queryFrontlet = frontlet(document, "query");
        tagFrontlet = frontlet(document, "tag");
        assertFrontletValues(queryFrontlet, "1", "2");
        assertFrontletValues(tagFrontlet, "1", "20");
        assertThat(SameFrontlet.lastStep).isEqualTo("20");
    }

    private void assertFrontletValues(Element frontlet, String pipeline, String step) {
        assertThat(frontlet.querySelector(".pipeline").getInnerText()).isEqualTo(pipeline);
        assertThat(frontlet.querySelector(".step").getInnerText()).isEqualTo(step);
    }

    private Element frontlet(Document document, String containerId) {
        var container = document.getElementById(containerId);
        assertThat(container).describedAs(document.asString()).isNotNull();
        var frontlet = container.querySelector("section");
        assertThat(frontlet).describedAs(document.asString()).isNotNull();
        return frontlet;
    }
}
