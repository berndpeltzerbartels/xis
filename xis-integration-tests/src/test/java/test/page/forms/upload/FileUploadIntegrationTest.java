package test.page.forms.upload;

import one.xis.UploadConfiguration;
import one.xis.UploadedFile;
import one.xis.context.HttpTestRequest;
import one.xis.context.HttpTestResponse;
import one.xis.context.IntegrationTestContext;
import one.xis.http.ContentType;
import one.xis.http.HttpMethod;
import one.xis.http.RestControllerService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FileUploadIntegrationTest {

    @Test
    void formUploadPassesFileBytesThroughIntegrationTestBridge() {
        var context = IntegrationTestContext.builder()
                .withSingleton(FileUploadPage.class)
                .build();
        var client = context.openPage("/file-upload.html");

        client.getDocument().getInputElementById("description").setValue("notes");
        client.getDocument().getInputElementById("attachment").setFile(
                "notes.txt",
                "text/plain",
                "Hello upload".getBytes(StandardCharsets.UTF_8)
        );
        client.getDocument().getElementById("save").click();

        assertThat(client.getDocument().getElementById("result").getInnerText())
                .isEqualTo("notes:notes.txt:Hello upload");
        assertThat(client.getDocument().getElementById("upload-form").getAttribute("enctype"))
                .isEqualTo("multipart/form-data");
    }

    @Test
    void multipartRequestLimitCountsAllUploadedFiles() {
        var context = IntegrationTestContext.builder()
                .withSingleton(FileUploadPage.class)
                .withSingleton(new SmallUploadConfiguration())
                .build();
        var response = new HttpTestResponse();

        context.getSingleton(RestControllerService.class).doInvocation(new HttpTestRequest(
                HttpMethod.POST,
                "/not-found",
                "{}",
                Map.of("Content-Type", ContentType.MULTIPART_FORM_DATA.getValue()),
                Map.of(
                        "attachment", List.of(new UploadedFile("attachment", "one.txt", "text/plain", bytes(8))),
                        "secondAttachment", List.of(new UploadedFile("secondAttachment", "two.txt", "text/plain", bytes(8)))
                )
        ), response);

        assertThat(response.getStatusCode()).isEqualTo(413);
    }

    @Test
    void uploadAnnotationCanRaiseSingleFileLimitWithinRequestLimit() {
        var context = IntegrationTestContext.builder()
                .withSingleton(FileUploadPage.class)
                .withSingleton(new SmallFileLimitConfiguration())
                .build();
        var client = context.openPage("/file-upload.html");

        client.getDocument().getInputElementById("description").setValue("larger");
        client.getDocument().getInputElementById("attachment").setFile(
                "larger.txt",
                "text/plain",
                bytes(16)
        );
        client.getDocument().getElementById("save").click();

        assertThat(client.getDocument().getElementById("result").getInnerText())
                .isEqualTo("larger:larger.txt:" + "x".repeat(16));
    }

    @Test
    void uploadFieldLimitViolationReturnsValidationMessages() {
        var context = IntegrationTestContext.builder()
                .withSingleton(FileUploadPage.class)
                .withSingleton(new SmallFileLimitConfiguration())
                .build();
        var client = context.openPage("/file-upload.html");

        client.getDocument().getInputElementById("description").setValue("too-large");
        client.getDocument().getInputElementById("attachment").setFile(
                "too-large.txt",
                "text/plain",
                bytes(80)
        );
        client.getDocument().getElementById("save").click();

        assertThat(client.getDocument().getElementById("result").getInnerText()).isEmpty();
        assertThat(client.getDocument().getElementById("attachment-message").getInnerText())
                .isEqualTo("Datei zu groß");
        assertThat(client.getDocument().getElementById("attachment").getAttribute("class"))
                .contains("error");
    }

    private static byte[] bytes(int count) {
        return "x".repeat(count).getBytes(StandardCharsets.UTF_8);
    }

    private static class SmallUploadConfiguration implements UploadConfiguration {

        @Override
        public long getMaxFileSize() {
            return 10;
        }

        @Override
        public long getMaxRequestSize() {
            return 12;
        }
    }

    private static class SmallFileLimitConfiguration implements UploadConfiguration {

        @Override
        public long getMaxFileSize() {
            return 10;
        }

        @Override
        public long getMaxRequestSize() {
            return 1024;
        }
    }
}
