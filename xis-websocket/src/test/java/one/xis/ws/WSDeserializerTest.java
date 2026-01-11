package one.xis.ws;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import one.xis.gson.GsonFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WSDeserializerTest {

    private Gson gson;

    @BeforeEach
    void setUp() {
        gson = new GsonFactory().gsonProvider(new GsonBuilder().create()).getGson();
    }

    @Test
    void testDeserializeWSClientRequest() {
        String json = """
                {
                  "path": "/xis/page/model",
                  "headers": {
                    "messageId": "msg-123",
                    "accessToken": "token-abc"
                  },
                  "parameters": {
                    "pageId": "/products/*.html"
                  },
                  "body": {
                    "pathVariables": {
                      "productId": "12345"
                    },
                    "queryParameters": {
                      "ref": "homepage"
                    }
                  }
                }
                """;

        WSClientRequest request = gson.fromJson(json, WSClientRequest.class);

        assertNotNull(request);
        assertEquals("/xis/page/model", request.getPath());
        assertNotNull(request.getBody());
        assertEquals("12345", request.getBody().getPathVariables().get("productId"));
        assertEquals("homepage", request.getBody().getQueryParameters().get("ref"));
        assertEquals("/products/*.html", request.getParameters().get("pageId"));
    }

    @Test
    void testDeserializeWSResourceRequest() {
        String json = """
                {
                  "path": "/xis/page/head",
                  "headers": {
                    "messageId": "msg-456",
                    "lastModifiedEpochMilli": 1609459200000
                  },
                  "parameters": {
                    "pageId": "main"
                  }
                }
                """;

        WSResourceRequest request = gson.fromJson(json, WSResourceRequest.class);

        assertNotNull(request);
        assertEquals("/xis/page/head", request.getPath());
        assertEquals("main", request.getParameters().get("pageId"));
    }

    @Test
    void testDeserializeWSClientRequestWithEmptyClientRequest() {
        String json = """
                {
                  "path": "/xis/page/config",
                  "headers": {},
                  "parameters": {},
                  "body": {}
                }
                """;

        WSClientRequest request = gson.fromJson(json, WSClientRequest.class);

        assertNotNull(request);
        assertEquals("/xis/page/config", request.getPath());
        assertNotNull(request.getBody());
    }

    @Test
    void testDeserializeWSClientRequestWithFormData() {
        String json = """
                {
                  "path": "/xis/form/action",
                  "headers": {
                    "messageId": "msg-789",
                    "accessToken": "token-xyz"
                  },
                  "parameters": {},
                  "body": {
                    "formBinding": "userForm",
                    "action": "save",
                    "formData": {
                      "username": "john_doe",
                      "email": "john@example.com"
                    }
                  }
                }
                """;

        WSClientRequest request = gson.fromJson(json, WSClientRequest.class);

        assertNotNull(request);
        assertEquals("/xis/form/action", request.getPath());
        assertNotNull(request.getBody());
        assertEquals("userForm", request.getBody().getFormBinding());
        assertEquals("save", request.getBody().getAction());
        assertEquals("john_doe", request.getBody().getFormData().get("username"));
        assertEquals("john@example.com", request.getBody().getFormData().get("email"));
    }

    @Test
    void testDeserializeWSResourceRequestWithLastModified() {
        String json = """
                {
                  "path": "/bundle.min.js",
                  "headers": {
                    "messageId": "msg-bundle",
                    "lastModifiedEpochMilli": 1609459200000
                  },
                  "parameters": {}
                }
                """;

        WSResourceRequest request = gson.fromJson(json, WSResourceRequest.class);

        assertNotNull(request);
        assertEquals("/bundle.min.js", request.getPath());
        assertEquals(1609459200000L, request.getHeaders().getLastModifiedAsEpochMilli());
    }
}
