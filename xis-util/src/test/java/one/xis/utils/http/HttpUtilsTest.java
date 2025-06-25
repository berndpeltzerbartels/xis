package one.xis.utils.http;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpUtilsTest {

    @Test
    void appendQueryParameters_toUrlWithoutQuery() {
        String url = "http://example.com/path";
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("param1", "value1");
        params.put("param2", 123);

        String expected = "http://example.com/path?param1=value1&param2=123";
        String actual = HttpUtils.appendQueryParameters(url, params);

        assertEquals(expected, actual);
    }

    @Test
    void appendQueryParameters_toUrlWithExistingQuery() {
        String url = "http://example.com/path?existing=true";
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("param1", "value1");
        params.put("param2", "value2");

        String expected = "http://example.com/path?existing=true&param1=value1&param2=value2";
        String actual = HttpUtils.appendQueryParameters(url, params);

        assertEquals(expected, actual);
    }

    @Test
    void appendQueryParameters_toUrlWithExistingQueryEndingInAmpersand() {
        String url = "http://example.com/path?existing=true&";
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("param1", "value1");

        String expected = "http://example.com/path?existing=true&param1=value1";
        String actual = HttpUtils.appendQueryParameters(url, params);

        assertEquals(expected, actual);
    }

    @Test
    void appendQueryParameters_withEmptyParameters() {
        String url = "http://example.com/path";
        Map<String, Object> params = Collections.emptyMap();

        // The current implementation adds a '?' even if there are no parameters.
        String expected = "http://example.com/path?";
        String actual = HttpUtils.appendQueryParameters(url, params);

        assertEquals(expected, actual);
    }

    @Test
    void appendQueryParameters_withEmptyParametersToUrlWithQuery() {
        String url = "http://example.com/path?a=1";
        Map<String, Object> params = Collections.emptyMap();

        // The current implementation adds a '&' even if there are no parameters.
        String expected = "http://example.com/path?a=1&";
        String actual = HttpUtils.appendQueryParameters(url, params);

        assertEquals(expected, actual);
    }

    @Test
    void appendQueryParameters_toUrlEndingInQuestionMark() {
        String url = "http://example.com/path?";
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("param1", "value1");

        // The current implementation adds an extra '&' because the url does not end with it.
        String expected = "http://example.com/path?&param1=value1";
        String actual = HttpUtils.appendQueryParameters(url, params);

        assertEquals(expected, actual);
    }
}