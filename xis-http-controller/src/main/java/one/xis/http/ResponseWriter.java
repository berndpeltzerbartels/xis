package one.xis.http;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import one.xis.context.Component;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.TypeUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Component
class ResponseWriter {
    private final Gson gson;

    ResponseWriter(Gson gson) {
        this.gson = gson;
    }

    void write(Object returnValue, Method method, HttpRequest request, HttpResponse response) {
        if (returnValue instanceof ResponseEntity<?> responseEntity) {
            response.setStatusCode(responseEntity.getStatusCode());
            for (var headerName : responseEntity.getHeaderNames()) {
                for (var headerValue : responseEntity.getHeaders(headerName)) {
                    response.addHeader(headerName, headerValue);
                }
            }

            returnValue = responseEntity.getBody();
        }
        if (method.isAnnotationPresent(ResponseHeader.class)) {
            var annotation = method.getAnnotation(ResponseHeader.class);
            if ("Content-Type".equalsIgnoreCase(annotation.name())) {
                response.setContentType(ContentType.fromValue(annotation.value()));
            } else {
                response.addHeader(annotation.name(), annotation.value());
            }
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

        if (response.getContentType() == null) {
            determineContentType(returnValue, method, request, response);
        }
        // may be it contains content type information. So we do this after checking the suffix
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
                response.setContentType(ContentType.TEXT_HTML_UTF8);
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
            response.setContentType(ContentType.JSON_UTF8);
        }

    }

    private void setResponseBody(Object returnValue, HttpResponse response) {
        switch (response.getContentType()) {
            case JSON_UTF8:
                var json = toJsonBytes(returnValue);
                // JSON ist per Definition UTF-8, daher ist getBytes() hier sicher.
                response.setBody(json);
                break;
            case FORM_URLENCODED:
                String formData;
                if (returnValue instanceof Map) {
                    formData = toUrlEncoded((Map<?, ?>) returnValue);
                } else if (!TypeUtils.isSimple(returnValue.getClass())) {
                    formData = toUrlEncoded(returnValue);
                } else {
                    formData = String.valueOf(returnValue);
                }
                var formBytes = formData.getBytes(StandardCharsets.UTF_8);
                response.setBody(formBytes);
                break;
            case APPLICATION_OCTET_STREAM:
                if (returnValue == null) {
                    response.setBody(new byte[0]);
                } else if (returnValue instanceof byte[] bytes) {
                    response.setBody(bytes);
                } else {
                    var bytes = String.valueOf(returnValue).getBytes(StandardCharsets.UTF_8);
                    response.setBody(bytes);
                }
                break;
            case JAVASCRIPT: // Explizit hinzufügen für Klarheit
            case TEXT_PLAIN:
            case TEXT_HTML_UTF8:
            case CSS:
            case XML:
            case SVG:
            default:
                if (returnValue == null) {
                    response.setBody(new byte[0]);
                    break;
                }

                byte[] textBody;
                if (returnValue instanceof CharSequence) {
                    textBody = returnValue.toString().getBytes(StandardCharsets.UTF_8);
                } else if (returnValue instanceof Map || returnValue instanceof Iterable) {
                    // Fallback für komplexe Typen, die als Text/HTML etc. gesendet werden
                    textBody = toJsonBytes(returnValue);
                } else {
                    textBody = String.valueOf(returnValue).getBytes();
                }

                response.setBody(textBody);
                break;
        }
    }

    private byte[] toJsonBytes(Object data) {
        return gson.toJson(data).getBytes(StandardCharsets.UTF_8);
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