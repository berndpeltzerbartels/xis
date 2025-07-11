package one.xis.http;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import one.xis.context.XISComponent;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.TypeUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@XISComponent
class ResponseWriter {
    private final Gson gson;

    ResponseWriter(Gson gson) {
        this.gson = gson;
    }

    void write(Object returnValue, Method method, HttpRequest request, HttpResponse response) {
        if (returnValue instanceof ResponseEntity<?> responseEntity) {
            response.setStatusCode(responseEntity.getStatusCode());
            for (var headerName : responseEntity.getHeaders().keySet()) {
                var headerValues = responseEntity.getHeaders().get(headerName);
                for (String headerValue : headerValues) {
                    response.addHeader(headerName, headerValue);
                }
            }
            returnValue = responseEntity.getBody();
        }
        if (returnValue == null) {
            if (response.getStatusCode() == null) {
                response.setStatusCode(204); // No Content
            }
            return;
        }

        if (response.getStatusCode() == null) {
            response.setStatusCode(200);
        }

        determineContentType(returnValue, method, request, response);
        setResponseBody(returnValue, response);
    }

    private void determineContentType(Object returnValue, Method method, HttpRequest request, HttpResponse response) {

        Produces produces = method.getAnnotation(Produces.class);
        if (produces != null) {
            response.setContentType(produces.value());
            return;
        }
        if (response.getContentType() != null) {
            // Wenn bereits ein Content-Type gesetzt ist, verwenden wir diesen
            return;
        }

        // Content-Type basierend auf dem Suffix des Request-Pfads bestimmen
        switch (request.getSuffix().toLowerCase()) {
            case ".js":
                response.setContentType(ContentType.JAVASCRIPT);
                return;
            case ".css":
                response.setContentType(ContentType.CSS);
                return;
            case ".html":
            case ".htm":
                response.setContentType(ContentType.TEXT_HTML);
                return;
            case ".pdf":
                response.setContentType(ContentType.PDF);
                return;
            case ".xml":
                response.setContentType(ContentType.XML);
                return;
            case ".jpeg":
            case ".jpg":
                response.setContentType(ContentType.JPEG);
                return;
            case ".png":
                response.setContentType(ContentType.PNG);
                return;
            case ".gif":
                response.setContentType(ContentType.GIF);
                return;
            case ".svg":
                response.setContentType(ContentType.SVG);
                return;
            case ".zip":
                response.setContentType(ContentType.ZIP);
                return;
        }

        // Fallback-Logik, wenn @Produces nicht vorhanden ist
        if (one.xis.utils.lang.TypeUtils.isSimple(returnValue.getClass())) {
            response.setContentType(ContentType.TEXT_PLAIN);
        } else if (returnValue instanceof byte[]) {
            response.setContentType(ContentType.APPLICATION_OCTET_STREAM);
        } else {
            response.setContentType(ContentType.JSON);
        }

    }

    private void setResponseBody(Object returnValue, HttpResponse response) {
        switch (response.getContentType()) {
            case JSON:
                response.setBody(gson.toJson(returnValue));
                break;
            case FORM_URLENCODED:
                if (returnValue instanceof Map) {
                    response.setBody(toUrlEncoded((Map<?, ?>) returnValue));
                } else if (!TypeUtils.isSimple(returnValue.getClass())) {
                    response.setBody(toUrlEncoded(returnValue));
                } else {
                    response.setBody(String.valueOf(returnValue));
                }
                break;
            case APPLICATION_OCTET_STREAM:
                if (returnValue == null) {
                    response.setBody(new byte[0]); // Leerer Body für null
                } else if (returnValue instanceof byte[]) {
                    response.setBody((byte[]) returnValue);
                } else if (returnValue instanceof CharSequence) {
                    response.setBody(((CharSequence) returnValue).toString().getBytes(StandardCharsets.UTF_8));
                } else {
                    // Fallback oder Fehlerbehandlung, falls der Typ nicht passt
                    response.setBody(String.valueOf(returnValue).getBytes(StandardCharsets.UTF_8));
                }
                if (returnValue instanceof byte[]) {
                    response.setBody((byte[]) returnValue);
                } else if (returnValue instanceof CharSequence) {
                    response.setBody(((CharSequence) returnValue).toString().getBytes(StandardCharsets.UTF_8));
                } else {
                    // Fallback oder Fehlerbehandlung, falls der Typ nicht passt
                    response.setBody(String.valueOf(returnValue).getBytes(StandardCharsets.UTF_8));
                }
                break;
            case TEXT_PLAIN:
            case TEXT_HTML:
            default:
                if (returnValue == null) {
                    response.setBody("");
                } else if (returnValue instanceof CharSequence) {
                    response.setBody(returnValue.toString());
                } else if (returnValue instanceof Map || returnValue instanceof Iterable) {
                    // Für komplexe Objekte, die nicht direkt in Text umgewandelt werden können
                    response.setBody(gson.toJson(returnValue));
                } else {
                    // Fallback für andere Typen
                    response.setBody(String.valueOf(returnValue));
                }
                response.setBody(String.valueOf(returnValue));
                break;
        }
    }

    private String toUrlEncoded(Map<?, ?> data) {
        return data.entrySet().stream()
                .map(entry -> {
                    try {
                        String key = URLEncoder.encode(String.valueOf(entry.getKey()), StandardCharsets.UTF_8.name());
                        String value = URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8.name());
                        return key + "=" + value;
                    } catch (UnsupportedEncodingException e) {
                        // Sollte mit UTF-8 nicht passieren
                        throw new IllegalStateException("UTF-8 not supported", e);
                    }
                })
                .collect(Collectors.joining("&"));
    }


    private String toUrlEncoded(Object data) {
        return FieldUtil.getAllFields(data.getClass()).stream()
                .map(field -> {
                    try {
                        field.setAccessible(true);
                        SerializedName serializedName = field.getAnnotation(SerializedName.class);
                        String key = serializedName != null ? serializedName.value() : field.getName();
                        Object value = field.get(data);

                        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.name());
                        String encodedValue = URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8.name());

                        return encodedKey + "=" + encodedValue;
                    } catch (IllegalAccessException | UnsupportedEncodingException e) {
                        throw new IllegalStateException("Error during URL encoding of object", e);
                    }
                })
                .collect(Collectors.joining("&"));
    }
}