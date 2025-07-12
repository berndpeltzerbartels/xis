package one.xis.http;

import lombok.Getter;

import java.time.Duration;
import java.util.*;

@Getter
public class ResponseEntity<T> {

    private T body;
    private final int statusCode;
    private final Map<String, List<String>> headers = new HashMap<>();


    public ResponseEntity(T body, int statusCode) {
        this.body = body;
        this.statusCode = statusCode;
    }

    public ResponseEntity(int statusCode) {
        this(null, statusCode);
    }

    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<>(body, 200);
    }

    public static <T> ResponseEntity<T> created(T body) {
        return new ResponseEntity<>(body, 201);
    }

    public static <T> ResponseEntity<T> noContent() {
        return new ResponseEntity<>(204);
    }

    public static <T> ResponseEntity<T> badRequest() {
        return new ResponseEntity<>(400);
    }

    public static <T> ResponseEntity<T> notFound() {
        return new ResponseEntity<>(404);
    }

    public static ResponseEntity<?> status(int statusCode) {
        return new ResponseEntity<>(null, statusCode);
    }

    public static <T> ResponseEntity<T> status(int statusCode, T body) {
        return new ResponseEntity<>(body, statusCode);
    }

    public ResponseEntity<T> addHeader(String name, String value) {
        headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        return this;
    }

    public static ResponseEntity<?> redirect(String location) {
        ResponseEntity<?> response = new ResponseEntity<>(302);
        response.addHeader("Location", location);
        return response;
    }

    public ResponseEntity<T> addSecureCookie(String name, String value, Duration maxAge) {
        StringJoiner cookieValue = new StringJoiner("; ");
        cookieValue.add(name + "=" + value);
        cookieValue.add("HttpOnly");
        cookieValue.add("Secure");
        cookieValue.add("SameSite=Lax");
        cookieValue.add("Max-Age=" + maxAge.getSeconds());
        cookieValue.add("Path=/");

        headers.computeIfAbsent("Set-Cookie", k -> new ArrayList<>()).add(cookieValue.toString());

        return this;
    }

    public ResponseEntity<T> body(T body) {
        this.body = body;
        return this;
    }

}