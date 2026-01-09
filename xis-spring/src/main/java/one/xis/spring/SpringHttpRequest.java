package one.xis.spring;


import jakarta.servlet.http.HttpServletRequest;
import one.xis.http.ContentType;
import one.xis.http.HttpMethod;
import one.xis.http.HttpRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class SpringHttpRequest implements HttpRequest {

    private final HttpServletRequest request;
    private byte[] body;

    public SpringHttpRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String getPath() {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    @Override
    public Map<String, String> getQueryParameters() {
        return request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0]));
    }

    @Override
    public byte[] getBody() {
        if (body == null) {
            try {
                body = request.getInputStream().readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException("Could not read request body", e);
            }
        }
        return body;
    }

    @Override
    public String getBodyAsString() {
        return new String(getBody(), StandardCharsets.UTF_8);
    }

    @Override
    public ContentType getContentType() {
        String contentTypeHeader = request.getContentType();
        if (contentTypeHeader == null) {
            return null;
        }
        for (ContentType ct : ContentType.values()) {
            if (contentTypeHeader.toLowerCase().startsWith(ct.getValue().toLowerCase())) {
                return ct;
            }
        }
        return null;
    }

    @Override
    public int getContentLength() {
        return request.getContentLength();
    }

    @Override
    public String getHeader(String name) {
        return request.getHeader(name) != null ? request.getHeader(name) : "";
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.valueOf(request.getMethod().toUpperCase());
    }

    @Override
    public Map<String, String> getFormParameters() {
        if (ContentType.FORM_URLENCODED.getValue().equalsIgnoreCase(request.getContentType())) {
            return getQueryParameters(); // Servlet API combines query and form params in getParameterMap
        }
        return Map.of();
    }

    @Override
    public Locale getLocale() {
        return request.getLocale();
    }

    @Override
    public String getSuffix() {
        return HttpRequest.super.getSuffix();
    }

    @Override
    public String getRemoteHost() {
        return request.getRemoteHost();
    }

    @Override
    public void addHeader(String name, String value) {
        throw new UnsupportedOperationException("Cannot add header to HttpServletRequest");
    }
}