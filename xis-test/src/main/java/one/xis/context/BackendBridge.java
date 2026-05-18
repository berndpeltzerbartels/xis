package one.xis.context;

import lombok.RequiredArgsConstructor;
import one.xis.http.HttpMethod;
import one.xis.http.RestControllerService;
import one.xis.UploadedFile;
import one.xis.test.dom.TestFile;
import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class BackendBridge {
    private final RestControllerService restControllerService;

    public JavascriptResponse invokeBackend(String httpMethod, String uri, Map<String, String> headers, String body) {
        var response = new HttpTestResponse();
        restControllerService.doInvocation(new HttpTestRequest(HttpMethod.fromString(httpMethod), uri, body, headers), response);
        return JavascriptResponse.builder()
                .responseText(response.getBody())
                .status(response.getStatusCode())
                .headers(response.getHeaders())
                .build();
    }

    public JavascriptResponse invokeMultipartBackend(String httpMethod, String uri, Map<String, String> headers, String body, Value uploads) {
        var response = new HttpTestResponse();
        restControllerService.doInvocation(new HttpTestRequest(HttpMethod.fromString(httpMethod), uri, body, headers, uploadedFiles(uploads)), response);
        return JavascriptResponse.builder()
                .responseText(response.getBody())
                .status(response.getStatusCode())
                .headers(response.getHeaders())
                .build();
    }

    private Map<String, List<UploadedFile>> uploadedFiles(Value uploads) {
        if (uploads == null || !uploads.hasArrayElements()) {
            return Map.of();
        }
        Map<String, List<UploadedFile>> result = new HashMap<>();
        for (long i = 0; i < uploads.getArraySize(); i++) {
            Value upload = uploads.getArrayElement(i);
            String fieldName = upload.getMember("fieldName").asString();
            TestFile file = testFile(upload.getMember("file"));
            if (fieldName == null || fieldName.isBlank() || file == null) {
                continue;
            }
            var uploadedFile = new UploadedFile(fieldName, file.getName(), file.getType(), file.getBytes());
            result.computeIfAbsent(fieldName, ignored -> new ArrayList<>()).add(uploadedFile);
        }
        return result;
    }

    private TestFile testFile(Value fileValue) {
        if (fileValue == null || fileValue.isNull()) {
            return null;
        }
        if (fileValue.isHostObject()) {
            Object hostObject = fileValue.asHostObject();
            return hostObject instanceof TestFile file ? file : null;
        }
        if (fileValue.isProxyObject()) {
            Object proxyObject = fileValue.asProxyObject();
            return proxyObject instanceof TestFile file ? file : null;
        }
        return null;
    }
}
